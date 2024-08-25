package com.function;

import com.domain.Authentication;
import com.domain.DeleteQueueMapping;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.ExecutionContext;

import java.util.Optional;

public class Function {

    private final DeleteQueueMapping deleteQueueMapping;
    private final Authentication authentication;

    public Function() {
        this.deleteQueueMapping = new DeleteQueueMapping();
        this.authentication = new Authentication();
    }

    @FunctionName("deleteQueueMappings")
    public HttpResponseMessage deleteQueueMappings(
            @HttpTrigger(name = "req", methods = {HttpMethod.DELETE}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<DeleteRequest>> request,
            ExecutionContext context) {

        String authHeader = request.getHeaders().get("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body("Unauthorized access").build();
        }

        String[] credentials = extractCredentials(authHeader);
        if (credentials == null || !authentication.authenticate(credentials[0], credentials[1])) {
            return request.createResponseBuilder(HttpStatus.UNAUTHORIZED).body("Invalid credentials").build();
        }

        DeleteRequest deleteRequest = request.getBody().orElse(null);
        if (deleteRequest == null || !deleteRequest.isValid()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Invalid request data").build();
        }

        boolean success = deleteQueueMapping.deleteMapping(deleteRequest.getPublisherId(), deleteRequest.getConsumerQueueName(), deleteRequest.getEventType());
        return success ? request.createResponseBuilder(HttpStatus.OK).body("Mapping deleted successfully").build() 
                       : request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete mapping").build();
    }

    private String[] extractCredentials(String authHeader) {
        String base64Credentials = authHeader.substring("Basic ".length()).trim();
        String credentials = new String(java.util.Base64.getDecoder().decode(base64Credentials));
        final String[] values = credentials.split(":", 2);
        return values.length == 2 ? values : null;
    }
}
