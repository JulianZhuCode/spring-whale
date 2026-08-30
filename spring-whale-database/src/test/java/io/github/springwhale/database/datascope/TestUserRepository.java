package io.github.springwhale.database.datascope;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TestUserRepository extends JpaRepository<TestUser, Long>, JpaSpecificationExecutor<TestUser> {
}