package com.financeira.api.application.usecase.bank;

import com.financeira.api.application.dto.BankResponse;
import com.financeira.api.domain.repository.BankRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListBanksUseCase {

    private final BankRepository repository;

    public ListBanksUseCase(BankRepository repository) {
        this.repository = repository;
    }

    public List<BankResponse> execute(String userUid) {
        return repository.findAllByUserUid(userUid).stream()
                .map(BankResponse::from)
                .collect(Collectors.toList());
    }
}
