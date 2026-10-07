package com.secureagent.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.secureagent.server.entity.AgentSystemInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AgentSystemInfoRepository
        extends JpaRepository<AgentSystemInfo, Long> {
    List<AgentSystemInfo> findByComputerName(String computerName);
    
    @Query(
    	    value = "SELECT * FROM (" +
    	            "SELECT * FROM AGENT_SYSTEM_INFO " +
    	            "WHERE COMPUTER_NAME = :computerName " +
    	            "ORDER BY ID DESC" +
    	            ") WHERE ROWNUM = 1",
    	    nativeQuery = true
    	)
    	Optional<AgentSystemInfo> findFirstByComputerNameOrderByIdDesc(
    	        @Param("computerName") String computerName);
}