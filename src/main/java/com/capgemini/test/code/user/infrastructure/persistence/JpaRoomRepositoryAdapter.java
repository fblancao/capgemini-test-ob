package com.capgemini.test.code.user.infrastructure.persistence;

import com.capgemini.test.code.user.application.port.out.RoomRepository;
import org.springframework.stereotype.Component;

@Component
class JpaRoomRepositoryAdapter implements RoomRepository {

  private final RoomJpaRepository jpaRepository;

  JpaRoomRepositoryAdapter(RoomJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public boolean existsById(Long roomId) {
    return jpaRepository.existsById(roomId);
  }
}

