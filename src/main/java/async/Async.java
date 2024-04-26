package async;

import cn.hutool.core.date.SystemClock;
import cn.hutool.json.JSONUtil;
import common.RemoteClientFactory;
import common.SpringContextUtil;
import common.StaticContextInitializer;
import constant.Constant;
import context.ContextHolder;
import entity.CommonLog;
import entity.RemoteProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class Async {
    private static final Executor asyncExecutor = SpringContextUtil.getBean("asyncExecutor", Executor.class);

    public static <T> CompletableFuture<List<T>> zip(List<CompletableFuture<T>> sources) {
        CompletableFuture<Void> allOf = CompletableFuture
                .allOf(sources.toArray(new CompletableFuture[0]))
                .exceptionally(e -> null);
        return Optional.ofNullable(allOf).map(all -> all
                        .thenApplyAsync(v -> sources.stream().map(CompletableFuture::join).collect(Collectors.toList()), asyncExecutor)
                        .exceptionally(throwable -> {
                            log.error("Failed to zip", throwable);
                            return new ArrayList<>();
                        }))
                .orElse(CompletableFuture.completedFuture(new ArrayList<>()));
    }

    public static <T> CompletableFuture<Void> zipThenAccept(List<CompletableFuture<T>> sources, Consumer<List<T>> consumer) {
        CompletableFuture<List<T>> future = zip(sources);
        return future.thenAccept(consumer).exceptionally(e -> {
            log.error("Failed to accept", e);
            return null;
        });
    }

    public static <T, R> CompletableFuture<R> zipThenApply(List<CompletableFuture<T>> sources, Function<List<T>, R> function) {
        CompletableFuture<List<T>> future = zip(sources);
        return future.thenApply(function).exceptionally(e -> {
            log.error("Failed to apply", e);
            return null;
        });
    }

    public static <T> CompletableFuture<T> startAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncExecutor)
                .exceptionally(throwable -> {
                    log.error("Failed to startAsync", throwable);
                    return null;
                });
    }

    public static <T> CompletableFuture<Optional<T>> startAsyncOptional(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> Optional.ofNullable(supplier.get()), asyncExecutor)
                .exceptionally(throwable -> {
                    log.error("Failed to startAsync", throwable);
                    return Optional.empty();
                });
    }

    public static <T> CompletableFuture<T> just(T t) {
        return CompletableFuture.completedFuture(t);
    }

    public static <T> CompletableFuture<Optional<T>> justOptional(T t) {
        return CompletableFuture.completedFuture(Optional.ofNullable(t));
    }

    public static CompletableFuture<Void> empty() {
        return new CompletableFuture<>();
    }

    public static <T> Optional<T> emptyOptional() {
        return Optional.empty();
    }

    public static CompletableFuture<Optional<?>> emptyFuture() {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    public static <Req, Resp> CompletableFuture<Resp> invokeAsync(Req request, String methodName, Class<Resp> respClass) {
        RemoteProperties remoteProperties = StaticContextInitializer.getRemoteProperties();
        RemoteProperties.Server server = remoteProperties.getServers().stream()
                .filter(p -> StringUtils.equals(p.getName(), methodName)).findFirst().orElse(null);
        if (server == null) {
            throw new RuntimeException("No server found for method: " + methodName);
        }
        return within(CompletableFuture.supplyAsync(() -> {
                            ContextHolder.setLog(Constant.TIMESTAMP, String.valueOf(SystemClock.now()));
                            return respClass.cast(RemoteClientFactory.invokeMethod(server.getClient(), server.getMethod(), request));
                        }, asyncExecutor)
                        .exceptionally(throwable -> {
                            log.error("Failed to invoke method: {}", methodName, throwable);
                            throw new RuntimeException("Failed to invoke method: " + methodName, throwable);
                        })
                , server.getTimeout(), TimeUnit.SECONDS)
                .whenCompleteAsync((r, t) -> saveGrpcLog(request, server.getMethod(), r, t), asyncExecutor)
                .exceptionally(e -> {
                    if (e.getCause() instanceof TimeoutException) {
                        log.warn("Timeout to invoke method: {}", methodName);
                    }
                    return null;
                });
    }

    private static <Req, Resp> void saveGrpcLog(Req request, String method, Resp resp, Throwable t) {
        CommonLog.Builder builder = new CommonLog.Builder();
        builder.api(ContextHolder.getLog(Constant.API))
                .service(Constant.GRPC)
                .method(method)
                .request(JSONUtil.toJsonStr(request))
                .response(t == null ? JSONUtil.toJsonStr(resp) : StringUtils.EMPTY)
                .exception(t == null ? StringUtils.EMPTY : t.getMessage())
                .traceLogId(ContextHolder.getLog(Constant.TRACE_LOG_ID))
                .timestamp(ContextHolder.getLog(Constant.TIMESTAMP))
                .interval(SystemClock.now() - NumberUtils.toLong(ContextHolder.getLog(Constant.TIMESTAMP)));
        log.info(JSONUtil.toJsonStr(builder.build()));
    }

    public static <Req, Resp> CompletableFuture<Optional<Resp>> invokeAsyncOptional(Req request, String methodName, Class<Resp> respClass) {
        RemoteProperties remoteProperties = StaticContextInitializer.getRemoteProperties();
        RemoteProperties.Server server = remoteProperties.getServers().stream()
                .filter(p -> StringUtils.equals(p.getMethod(), methodName)).findFirst().orElse(null);
        if (server == null) {
            throw new RuntimeException("No server found for method: " + methodName);
        }
        return within(CompletableFuture.supplyAsync(() -> {
                                    ContextHolder.setLog(Constant.TIMESTAMP, String.valueOf(SystemClock.now()));
                                    return Optional.ofNullable(respClass.cast(RemoteClientFactory.invokeMethod(server.getClient(), server.getMethod(), request)));
                                },
                                asyncExecutor)
                        .exceptionally(throwable -> {
                            log.error("Failed to invoke method: {}", methodName, throwable);
                            throw new RuntimeException("Failed to invoke method: " + methodName, throwable);
                        })
                , server.getTimeout(), TimeUnit.SECONDS)
                .whenCompleteAsync((r, t) -> saveGrpcLog(request, server.getMethod(), r, t), asyncExecutor)
                .exceptionally(e -> {
                    if (e instanceof TimeoutException) {
                        log.warn("Timeout to invoke method: {}", methodName);
                    }
                    return Optional.empty();
                });
    }

    public static <T> CompletableFuture<T> within(CompletableFuture<T> future, long timeout, TimeUnit unit) {
        final CompletableFuture<T> timeoutFuture = timeoutAfter(timeout, unit);
        // 哪个先完成 就apply哪一个结果 这是一个关键的API
        return future.applyToEither(timeoutFuture, Function.identity());
    }

    public static <T> CompletableFuture<T> timeoutAfter(long timeout, TimeUnit unit) {
        CompletableFuture<T> result = new CompletableFuture<>();
        // timeout 时间后 抛出TimeoutException 类似于sentinel / watcher
        Delayer.getInstance().schedule(() -> result.completeExceptionally(new TimeoutException()), timeout, unit);
        return result;
    }

    public static <T> CompletableFuture<T> timeoutAfter(CompletableFuture<T> future, long timeout, TimeUnit unit) {
        CompletableFuture<T> result = new CompletableFuture<>();
        // CompletableFuture的cancel方法尝试取消执行。
        // 但是，如果CompletableFuture的计算已经完成，或者出于其他原因无法取消，那么这个方法将无法成功取消执行。
        // 在Java中，一旦一个线程开始执行，就无法安全地停止它，除非它自己决定停止。这就是为什么你看到CompletableFuture已经被取消，但是它的计算仍然执行完毕。
        // 这是Java设计的一部分，主要是为了线程安全。强行停止一个线程可能会导致程序处于不一致的状态
        Delayer.getInstance().schedule(() -> {
            result.completeExceptionally(new TimeoutException());
            future.cancel(true);
        }, timeout, unit);
        return result;
    }


    public static <R, T1, T2> CompletableFuture<R> zip(CompletableFuture<T1> cf1,
                                                       CompletableFuture<T2> cf2,
                                                       BiFunction<T1, T2, R> biFunction) {
        return cf1.thenCombineAsync(cf2, biFunction, asyncExecutor)
                .exceptionally(throwable -> {
                    log.error("Failed to zip", throwable);
                    return null;
                });
    }

    public static <R, T1, T2, T3> CompletableFuture<R> zip(CompletableFuture<T1> cf1,
                                                           CompletableFuture<T2> cf2,
                                                           CompletableFuture<T3> cf3,
                                                           Function3<T1, T2, T3, R> function3) {
        return CompletableFuture.allOf(cf1, cf2, cf3)
                .thenApplyAsync(v -> function3.apply(
                                cf1.join(), cf2.join(), cf3.join()),
                        asyncExecutor)
                .exceptionally(throwable -> {
                    log.error("Failed to zip", throwable);
                    return null;
                });
    }

    public static <R, T1, T2, T3, T4> CompletableFuture<R> zip(CompletableFuture<T1> cf1,
                                                               CompletableFuture<T2> cf2,
                                                               CompletableFuture<T3> cf3,
                                                               CompletableFuture<T4> cf4,
                                                               Function4<T1, T2, T3, T4, R> function4) {
        return CompletableFuture.allOf(cf1, cf2, cf3, cf4)
                .thenApplyAsync(v -> function4.apply(
                                cf1.join(), cf2.join(), cf3.join(), cf4.join()),
                        asyncExecutor)
                .exceptionally(throwable -> {
                    log.error("Failed to zip", throwable);
                    return null;
                });
    }

    public static <R, T1, T2, T3, T4, T5> CompletableFuture<R> zip(CompletableFuture<T1> cf1,
                                                                   CompletableFuture<T2> cf2,
                                                                   CompletableFuture<T3> cf3,
                                                                   CompletableFuture<T4> cf4,
                                                                   CompletableFuture<T5> cf5,
                                                                   Function5<T1, T2, T3, T4, T5, R> function5) {
        return CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5)
                .thenApplyAsync(v -> function5.apply(
                                cf1.join(), cf2.join(), cf3.join(), cf4.join(), cf5.join()),
                        asyncExecutor)
                .exceptionally(throwable -> {
                    log.error("Failed to zip", throwable);
                    return null;
                });
    }

    public static <R, T1, T2> CompletableFuture<Optional<R>> optionalZip(CompletableFuture<Optional<T1>> cf1,
                                                                         CompletableFuture<Optional<T2>> cf2,
                                                                         BiFunction<T1, T2, R> biFunction) {
        return cf1.thenCombineAsync(cf2, (p1, p2) -> Optional.ofNullable(biFunction.apply(p1.get(), p2.get())), asyncExecutor)
                .exceptionally(throwable -> {
                    log.error("Failed to zip", throwable);
                    return Optional.empty();
                });
    }

    public static <R, T1, T2, T3> CompletableFuture<Optional<R>> optionalZip(CompletableFuture<Optional<T1>> cf1,
                                                                             CompletableFuture<Optional<T2>> cf2,
                                                                             CompletableFuture<Optional<T3>> cf3,
                                                                             Function3<T1, T2, T3, R> function3) {
        return CompletableFuture.allOf(cf1, cf2, cf3)
                .thenApplyAsync(v -> Optional.ofNullable(function3.apply(
                                cf1.join().get(), cf2.join().get(), cf3.join().get())),
                        asyncExecutor)
                .exceptionally(throwable -> {
                    log.error("Failed to zip", throwable);
                    return Optional.empty();
                });
    }

    public static <R, T1, T2, T3, T4> CompletableFuture<Optional<R>> optionalZip(CompletableFuture<Optional<T1>> cf1,
                                                                                 CompletableFuture<Optional<T2>> cf2,
                                                                                 CompletableFuture<Optional<T3>> cf3,
                                                                                 CompletableFuture<Optional<T4>> cf4,
                                                                                 Function4<T1, T2, T3, T4, R> function4) {
        return CompletableFuture.allOf(cf1, cf2, cf3, cf4)
                .thenApplyAsync(v -> Optional.ofNullable(function4.apply(
                                cf1.join().get(), cf2.join().get(), cf3.join().get(), cf4.join().get())),
                        asyncExecutor)
                .exceptionally(throwable -> {
                    log.error("Failed to zip", throwable);
                    return Optional.empty();
                });
    }

    public static <R, T1, T2, T3, T4, T5> CompletableFuture<Optional<R>> optionalZip(CompletableFuture<Optional<T1>> cf1,
                                                                                     CompletableFuture<Optional<T2>> cf2,
                                                                                     CompletableFuture<Optional<T3>> cf3,
                                                                                     CompletableFuture<Optional<T4>> cf4,
                                                                                     CompletableFuture<Optional<T5>> cf5,
                                                                                     Function5<T1, T2, T3, T4, T5, R> function5) {
        return CompletableFuture.allOf(cf1, cf2, cf3, cf4, cf5)
                .thenApplyAsync(v -> Optional.ofNullable(function5.apply(
                                cf1.join().get(), cf2.join().get(), cf3.join().get(), cf4.join().get(), cf5.join().get())),
                        asyncExecutor)
                .exceptionally(throwable -> {
                    log.error("Failed to zip", throwable);
                    return Optional.empty();
                });
    }
}
