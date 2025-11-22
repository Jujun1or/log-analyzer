package academy.core;

public record PipelineConfig(
    int batchSize,
    int queueCapacity,
    int numConsumers
) {}
