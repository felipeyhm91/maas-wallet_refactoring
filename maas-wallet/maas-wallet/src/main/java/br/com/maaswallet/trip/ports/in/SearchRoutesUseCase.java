package br.com.maaswallet.trip.ports.in;

import br.com.maaswallet.trip.domain.model.RouteOption;
import java.util.List;

public interface SearchRoutesUseCase {
    List<RouteOption> search(Command command);

    record Command(
        String origin,
        String destination
    ) {}
}
