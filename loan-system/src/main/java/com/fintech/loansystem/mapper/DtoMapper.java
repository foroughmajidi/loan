package com.fintech.loansystem.mapper;

import com.fintech.loansystem.dto.LoanResponseDto;
import com.fintech.loansystem.model.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public interface DtoMapper {

    LoanResponseDto loanToLoanResponseDto(Loan loan);
}
