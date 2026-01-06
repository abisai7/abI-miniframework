package com.abidev.http;

import java.util.Map;

public record HandlerResult(int status, Map<String, String> headers, Object body) {

}
