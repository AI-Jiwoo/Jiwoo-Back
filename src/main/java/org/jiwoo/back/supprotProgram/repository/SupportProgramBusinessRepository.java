package org.jiwoo.back.supprotProgram.repository;

import org.jiwoo.back.supprotProgram.aggregate.entity.SupportProgramBusiness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportProgramBusinessRepository extends JpaRepository<SupportProgramBusiness, Integer> {
}
