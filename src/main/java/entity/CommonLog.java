package entity;

import lombok.Data;

@Data
public class CommonLog {
    private String verificationCode;
    private String api;
    private String service;
    private String method;
    private String interval;
    private String event;
    private String request;
    private String response;
    private String traceLogId;
    private String exception;
    private String timestamp;

    public static class Builder {
        private String verificationCode;
        private String api;
        private String service;
        private String method;
        private String interval;
        private String event;
        private String request;
        private String response;
        private String traceLogId;
        private String exception;
        private String timestamp;

        public Builder verificationCode(String verificationCode) {
            this.verificationCode = verificationCode;
            return this;
        }

        public Builder api(String api) {
            this.api = api;
            return this;
        }

        public Builder service(String service) {
            this.service = service;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder interval(long interval) {
            this.interval = String.valueOf(interval);
            return this;
        }

        public Builder event(String event) {
            this.event = event;
            return this;
        }

        public Builder request(String request) {
            this.request = request;
            return this;
        }

        public Builder response(String response) {
            this.response = response;
            return this;
        }

        public Builder traceLogId(String traceLogId) {
            this.traceLogId = traceLogId;
            return this;
        }

        public Builder exception(String exception) {
            this.exception = exception;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = String.valueOf(timestamp);
            return this;
        }

        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public CommonLog build() {
            CommonLog commonLog = new CommonLog();
            commonLog.setVerificationCode(this.verificationCode);
            commonLog.setApi(this.api);
            commonLog.setEvent(this.event);
            commonLog.setService(this.service);
            commonLog.setMethod(this.method);
            commonLog.setRequest(this.request);
            commonLog.setResponse(this.response);
            commonLog.setTraceLogId(this.traceLogId);
            commonLog.setInterval(this.interval);
            commonLog.setException(this.exception);
            commonLog.setTimestamp(this.timestamp);
            return commonLog;
        }

    }
}