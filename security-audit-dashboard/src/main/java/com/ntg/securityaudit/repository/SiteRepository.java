package com.ntg.securityaudit.repository;

import com.ntg.securityaudit.entity.Site;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

    @Query("select coalesce(avg(s.securityScore), 0) from Site s")
    Double averageSecurityScore();

    Optional<Site> findByNameIgnoreCase(String name);

}
