package lumi.insert.app.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import lumi.insert.app.core.entity.MemoView;

/**
 * Repository for {@link MemoView} entity.
 * @author KelvinKhodes
 * @since 1.0.0 
 */
@Repository
public interface MemoViewRepository extends JpaRepository<MemoView, String>{
    
    /**
     * Delete memo view related to memo
     * @param id
     * @return affected rows count
     */
    @Modifying
    @Query("DELETE FROM memo_views mv WHERE mv.memo.id = :id")
    int deleteMemoView(@Param("id") Long id);

}
