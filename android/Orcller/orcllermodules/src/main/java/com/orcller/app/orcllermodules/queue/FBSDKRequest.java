package com.orcller.app.orcllermodules.queue;

import android.os.Bundle;

import com.orcller.app.orcllermodules.error.APIError;
import com.orcller.app.orcllermodules.model.AbstractModel;

import org.json.JSONObject;

/**
 * Created by pisces on 11/9/15.
 */
public class FBSDKRequest {
    private Object target;
    private String graphPath;
    private Bundle parameters;
    private Class mappingClass;
    private CompleteHandler completeHandler;

    public FBSDKRequest(
            Object target,
            String graphPath,
            Bundle parameters,
            Class mappingClass,
            CompleteHandler completeHandler) {
        this.target = target;
        this.graphPath = graphPath;
        this.parameters = parameters;
        this.mappingClass = mappingClass;
        this.completeHandler = completeHandler;
    }

    public void clear() {
        target = null;
        graphPath = null;
        parameters = null;
        mappingClass = null;
        completeHandler = null;
    }

    public Object getTarget() {
        return target;
    }

    public String getGraphPath() {
        return graphPath;
    }

    public Bundle getParameters() {
        return parameters;
    }

    public Class getMappingClass() {
        return mappingClass;
    }

    public CompleteHandler getCompleteHandler() {
        return completeHandler;
    }

    public static class Builder {
        private Object target;
        private String graphPath;
        private Bundle parameters;
        private Class mappingClass;
        private CompleteHandler completeHandler;

        public FBSDKRequest build() {
            return new FBSDKRequest(target, graphPath, parameters, mappingClass, completeHandler);
        }

        public Builder setTarget(Object target) {
            this.target = target;
            return this;
        }

        public Builder setGraphPath(String graphPath) {
            this.graphPath = graphPath;
            return this;
        }

        public Builder setParameters(Bundle parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder setMappingClass(Class mappingClass) {
            this.mappingClass = mappingClass;
            return this;
        }

        public Builder setCompleteHandler(CompleteHandler completeHandler) {
            this.completeHandler = completeHandler;
            return this;
        }
    }

    public interface CompleteHandler {
        public void onComplete(JSONObject result, APIError error);
    }
}
