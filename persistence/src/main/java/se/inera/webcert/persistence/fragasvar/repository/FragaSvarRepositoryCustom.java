package se.inera.webcert.persistence.fragasvar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.inera.webcert.persistence.fragasvar.model.FragaSvar;

public interface FragaSvarRepositoryCustom extends FragaSvarFilteredRepositoryCustom {
    /**
     * Should return a list of {@link FragaSvar} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with {@link se.inera.webcert.persistence.fragasvar.model.Status.CLOSED}. The result is NOT ordered.
     *
     * @param enhetsIds
     * @return A list of {@link FragaSvar} matching the search criteria. If no entities are found, this method returns
     * an empty list.
     */
    @Query("SELECT fs FROM FragaSvar fs WHERE fs.vardperson.enhetsId IN (:idList) AND fs.status <> 'CLOSED'")
    List<FragaSvar> findByEnhetsId(@Param("idList") List<String> enhetsIds);

    /**
     * Should return a count of {@link FragaSvar} entities in the repository that has an enhetsId matching one of the
     * supplied list of id's. Is also discards any entity with {@link se.inera.webcert.persistence.fragasvar.model.Status.CLOSED}.
     *
     * @param enhetsIds List of hsa unit id's that should match the counted fraga svar entities
     * @return A count of {@link FragaSvar} matching the search criteria.
     */
    @Query("SELECT count(fs) FROM FragaSvar fs WHERE fs.vardperson.enhetsId IN (:idList) AND fs.status <> 'CLOSED'")
    Long countUnhandledForEnhetsIds(@Param("idList") List<String> enhetsIds);

    /**
     * Should return a list that contains an array with enhets id and the number of unhandled {@link FragaSvar} entities for that enhet.
     *
     * @param enhetsIds List of hsa unit id's that should match the counted fraga svar entities.
     * @return A list that contains an array with enhets id and the number of unhandled for that enhet.
     */
    @Query("SELECT DISTINCT fs.vardperson.enhetsId, count(fs.vardperson.enhetsId) FROM FragaSvar fs WHERE fs.vardperson.enhetsId IN (:idList) AND fs.status <> 'CLOSED' GROUP BY fs.vardperson.enhetsId")
    List<Object[]> countUnhandledGroupedByEnhetIds(@Param("idList") List<String> enhetsIds);

    /**
     * Returns a list of all unique hsaId and name (of vardperson who signed the certificate the FragaSvar is linked to) where matches the supplied id.
     *
     * @param enhetsid
     * @return A list of Object[] where the first [0] value is the HsaId and the second [1] is the name
     */
    @Query("SELECT DISTINCT fs.vardperson.hsaId, fs.vardperson.namn FROM FragaSvar fs WHERE fs.vardperson.enhetsId IN (:idList) ORDER BY fs.vardperson.namn ASC")
    List<Object[]> findDistinctFragaSvarHsaIdByEnhet(@Param("idList") List<String> enhetsIds);

    @Query("SELECT count(fs) FROM FragaSvar fs WHERE fs.intygsReferens.intygsId = :intygsId")
    Long countByIntyg(@Param("intygsId") String intygsId);
    
    @Query("SELECT count(fs) FROM FragaSvar fs WHERE fs.intygsReferens.intygsId = :intygsId AND fs.status = 'CLOSED'")
    Long countHandledByIntyg(@Param("intygsId") String intygsId);
    
    @Query("SELECT count(fs) FROM FragaSvar fs WHERE fs.intygsReferens.intygsId = :intygsId AND fs.status = 'ANSWERED' AND fs.svarsText IS NOT NULL")
    Long countAnsweredByIntyg(@Param("intygsId") String intygsId);
    
    @Query("SELECT count(fs) FROM FragaSvar fs WHERE fs.intygsReferens.intygsId = :intygsId AND fs.status = 'CLOSED' AND fs.svarsText IS NOT NULL")
    Long countHandledAndAnsweredByIntyg(@Param("intygsId") String intygsId);

    /**
     * Should return a list of {@link FragaSvar} entities in the repository related to the specified intygsId.
     *
     * @param intygsId
     * @return A list of {@link FragaSvar} matching the search criteria. If no entities are found, this method returns
     * an empty list.
     */
    List<FragaSvar> findByIntygsReferensIntygsId(String intygsId);

    /**
     * Should return a {@link FragaSvar} matching the search criteria.
     *
     * @param externReferens
     * @return
     */
    FragaSvar findByExternReferens(String externReferens);

    /**
     * Should return a {@link FragaSvar} matching the search criteria.
     *
     * @param frageText
     * @return
     */
    List<FragaSvar> findByFrageTextLike(String frageText);

    /**
     * Should return a {@link FragaSvar} matching the search criteria.
     *
     * @param svarsText
     * @return
     */
    List<FragaSvar> findBySvarsTextLike(String svarsText);

}
