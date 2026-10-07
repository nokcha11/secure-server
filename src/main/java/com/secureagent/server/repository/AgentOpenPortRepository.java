package com.secureagent.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secureagent.server.entity.AgentOpenPort;

public interface AgentOpenPortRepository
        extends JpaRepository<AgentOpenPort, Long> {

    List<AgentOpenPort>
            findByComputerNameOrderByLocalPortAsc(
                    String computerName);

    void deleteByComputerName(String computerName);
}