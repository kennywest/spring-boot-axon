package info.novatec.axon.account.query;

import info.novatec.axon.BaseEvent;
import info.novatec.axon.account.event.AccountCreatedEvent;
import info.novatec.axon.account.event.MoneyDepositedEvent;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.modelling.command.Repository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BankAccounts {

    private final Map<String, BankAccountDto> accounts = new HashMap<>();
    private final Repository repository;
    private final EventStore eventStore;

    public BankAccounts(Repository repository, EventStore eventStore) {
        this.repository = repository;
        this.eventStore = eventStore;
    }

    @EventSourcingHandler
    protected void on(AccountCreatedEvent event) {
        this.accounts.put(event.id, new BankAccountDto(event.id, event.balance, event.accountCreator));
    }

    @EventSourcingHandler
    protected void on(MoneyDepositedEvent event) {
        this.accounts.get(event.id).deposit(event.amount);
    }

    @QueryHandler
    public BankAccountDto handle(BankAccountQuery query) {
        //System.out.println(repository.load(query.id));
        BankAccountDto account = accounts.get(query.id);

        if (account == null) {
            this.eventStore.readEvents(query.id).asStream().forEach(msg -> {
                BaseEvent event = (BaseEvent) ((DomainEventMessage) msg).getPayload();
            });
        }

        return account;
    }
}
