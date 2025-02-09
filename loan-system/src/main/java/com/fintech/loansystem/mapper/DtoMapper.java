package com.fintech.loansystem.mapper;

import com.fintech.loansystem.dto.LoanReqResponseDto;
import com.fintech.loansystem.dto.LoanResponseDto;
import com.fintech.loansystem.dto.UserDto;
import com.fintech.loansystem.model.Loan;
import com.fintech.loansystem.model.LoanRequest;
import com.fintech.loansystem.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DtoMapper {

    LoanResponseDto loanToLoanResponseDto(Loan loan);

    LoanReqResponseDto loanRequestToLoanResponseDto(LoanRequest loanRequest);

    UserDto userToUserDto(User user);


}
