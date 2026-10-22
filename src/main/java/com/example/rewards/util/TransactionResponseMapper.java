package com.example.rewards.util;

import com.example.rewards.dto.TransactionResponse;
import com.example.rewards.entity.Transaction;

import java.util.List;

/** Plain mapping helper; a generated mapper is overkill for four fields. */
public final class TransactionResponseMapper {

    private TransactionResponseMapper() {
    }

    public static List<TransactionResponse> toDTOList(List<Transaction> entities) {
        return entities.stream().map(TransactionResponse::from).toList();
    }
}
