package org.openmuc.framework.lib.rest1.service;

import java.util.concurrent.CompletableFuture;

public interface ASyncService {
    public CompletableFuture<String> calculateSoh(Long id, String strId);
}
