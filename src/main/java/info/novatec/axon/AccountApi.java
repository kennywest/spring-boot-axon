package info.novatec.axon;

import info.novatec.axon.account.BankAccount;
import info.novatec.axon.account.command.CloseAccountCommand;
import info.novatec.axon.account.command.CreateAccountCommand;
import info.novatec.axon.account.command.DepositMoneyCommand;
import info.novatec.axon.account.command.WithdrawMoneyCommand;
import info.novatec.axon.account.query.BankAccountDto;
import info.novatec.axon.account.query.BankAccountQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.axonframework.modelling.command.Repository;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RequestMapping("/accounts")
@RestController
public class AccountApi {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;

    private final EventStore eventStore;

    private final Repository repository;

    public AccountApi(CommandGateway commandGateway, QueryGateway queryGateway, EventStore eventStore, Repository repository) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.eventStore = eventStore;
        this.repository = repository;
    }

    @GetMapping("{id}/events")
    public List<Object> getEvents(@PathVariable("id") String id) {
        return eventStore.readEvents(id).asStream().map(s -> s.getPayload()).collect(Collectors.toList());
    }

    @GetMapping("{id}/events/last")
    public Object getLastEvent(@PathVariable("id") String id) {
        return eventStore.lastSequenceNumberFor(id).map(l -> eventStore.readEvents(id, l).asStream().findFirst()).orElse(null);
    }

    @GetMapping("/{id}")
    public BankAccountDto get(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        return queryGateway.query(new BankAccountQuery(id), ResponseTypes.instanceOf(BankAccountDto.class)).get();
    }


    @PostMapping
    public CompletableFuture<String> createAccount(@RequestBody AccountOwner user) {
        String id = UUID.randomUUID().toString();
        return commandGateway.send(new CreateAccountCommand(id, user.name));
    }

    @PutMapping(path = "{accountId}/balance")
    public CompletableFuture<String> deposit(@RequestBody double ammount, @PathVariable String accountId) {
        if (ammount > 0) {
            return commandGateway.send(new DepositMoneyCommand(accountId, ammount));
        } else {
            return commandGateway.send(new WithdrawMoneyCommand(accountId, -ammount));
        }
    }

    @DeleteMapping("{id}")
    public CompletableFuture<String> delete(@PathVariable String id) {
        return commandGateway.send(new CloseAccountCommand(id));
    }

    @ExceptionHandler(AggregateNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void notFound() {
    }

    @ExceptionHandler(BankAccount.InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String insufficientBalance(BankAccount.InsufficientBalanceException exception) {
        return exception.getMessage();
    }

    static class AccountOwner {
        public String name;
    }
}
