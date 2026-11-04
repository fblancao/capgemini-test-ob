package com.capgemini.test.code.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface RoomJpaRepository extends JpaRepository<RoomEntity, Long> {
}

