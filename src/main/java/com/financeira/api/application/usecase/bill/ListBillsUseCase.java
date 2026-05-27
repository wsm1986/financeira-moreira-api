package com.financeira.api.application.usecase.bill;

import com.financeira.api.application.dto.BillResponse;
import com.financeira.api.domain.repository.BillRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListBillsUseCase {

    private final BillRepository repository;

    public ListBillsUseCase(BillRepository repository) {
        this.repository = repository;
    }

    public List<BillResponse> execute(String userUid) {
        return repository.findAllByUserUid(userUid).stream()
                .map(BillResponse::from)
                .collect(Collectors.toList());
    }
}
