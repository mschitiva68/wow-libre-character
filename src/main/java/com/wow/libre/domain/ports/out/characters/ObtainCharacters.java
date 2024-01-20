package com.wow.libre.domain.ports.out.characters;

import com.wow.libre.domain.model.Character;

import java.util.List;
import java.util.Optional;

public interface ObtainCharacters {
  List<Character> getCharacters(Long accountId, String transactionId);

  Optional<Character> getCharacter(Long guid, Long accountId, String transactionId);

  Optional<Character> getCharacter(Long guid, String transactionId);

  List<Character> getCharactersOnline(String transactionId);
}
