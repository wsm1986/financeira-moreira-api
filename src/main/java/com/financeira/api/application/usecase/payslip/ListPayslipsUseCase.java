package com.financeira.api.application.usecase.payslip;

import com.financeira.api.application.dto.PayslipResponse;
import com.financeira.api.domain.repository.PayslipRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListPayslipsUseCase {

    private final PayslipRepository repository;

    public ListPayslipsUseCase(PayslipRepository repository) {
        this.repository = repository;
    }

    public List<PayslipResponse> execute(String userUid) {
        return repository.findAllByUserUid(userUid).stream()
                .map(PayslipResponse::from)
                .collect(Collectors.toList());
    }
}
