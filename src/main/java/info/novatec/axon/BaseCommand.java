package info.novatec.axon;

import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.springframework.util.Assert;

public class BaseCommand<T> {
    @TargetAggregateIdentifier
    public final T id;

    public BaseCommand(T id) {
        Assert.notNull(id, "Id cannot be null");
        this.id = id;
    }
}
