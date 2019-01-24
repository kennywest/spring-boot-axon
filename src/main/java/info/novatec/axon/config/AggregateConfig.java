package info.novatec.axon.config;

import info.novatec.axon.account.BankAccount;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.modelling.command.Repository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AggregateConfig {

    @Bean
    public Repository<BankAccount> repository(EventStore eventStore) {
        return EventSourcingRepository.builder(BankAccount.class).eventStore(eventStore).build();
    }
}
