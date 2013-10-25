package se.inera.webcert.persistence.fragasvar.repository;

import org.joda.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import se.inera.webcert.persistence.fragasvar.model.*;
import se.inera.webcert.persistence.fragasvar.repository.util.FragaSvarTestUtil;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by pehr on 10/21/13.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:repository-context.xml" })
@ActiveProfiles("dev")
@Transactional
public class FragaSvarFilteredRepositoryCustomTest {

    @Autowired
    private FragaSvarRepository fragasvarRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    public void testFilterFragaFromWC() {
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);
        filter.setQuestionFromWC(true);
        FragaSvarTestUtil.populateFragaSvar(filter, 1,fragasvarRepository);
        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        Assert.assertTrue(fsList.size() == 1);
        Assert.assertTrue(fsList.get(0).getFrageStallare().equalsIgnoreCase("WC"));
        Assert.assertTrue(fsList.get(0).getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));
        fragasvarRepository.deleteAll();
    }

    @Test
    public void testFilterFragaFromWCWithPaging() {
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);
        filter.setQuestionFromWC(true);
        FragaSvarTestUtil.populateFragaSvar(filter, 10,fragasvarRepository);
        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter,new PageRequest(4,3));

        Assert.assertTrue(fsList.size()==3);
        fragasvarRepository.deleteAll();
    }

    @Test
    public void testFilterHsaId() {
        String hsaid =  "HSA-User-123";
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);

        filter.setHsaId(hsaid);
        FragaSvarTestUtil.populateFragaSvar(filter, 1,fragasvarRepository);
        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        Assert.assertTrue(fsList.size() == 1);
        Assert.assertTrue(fsList.get(0).getVardperson().getHsaId().equals(hsaid));
        Assert.assertTrue(fsList.get(0).getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));
        fragasvarRepository.deleteAll();
    }

    @Test
    public void testFilterChangedAfter() {
        LocalDateTime changeDateFrom =  new LocalDateTime( 2013, 06, 15,0,0);
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);

        filter.setChangedFrom(changeDateFrom);
        FragaSvarTestUtil.populateFragaSvar(filter, 1,fragasvarRepository);

        filter.setChangedFrom(filter.getChangedFrom().minusDays(3));

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        //Assert.assertTrue(fsList.size() == 1);
        fragasvarRepository.deleteAll();
    }

    @Test
    public void testFilterChangedTo() {
        LocalDateTime changeDateTo =  new LocalDateTime( 2013, 06, 15,0,0);
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);

        filter.setChangedTo(changeDateTo);
        FragaSvarTestUtil.populateFragaSvar(filter, 1,fragasvarRepository);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        Assert.assertTrue(fsList.size() == 1);
        //Assert.assertTrue(fsList.get(0).getSenasteHandelseDatum().equals(changeDateTo));
        Assert.assertTrue(fsList.get(0).getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));

        fragasvarRepository.deleteAll();
    }
    @Test
    public void testFilterChangedFrom() {
        LocalDateTime changeDateFrom =  new LocalDateTime( 2013, 06, 15,0,0);
        FragaSvarFilter filter = new FragaSvarFilter();

        filter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);
        filter.setChangedFrom(changeDateFrom);
        FragaSvarTestUtil.populateFragaSvar(filter, 1,fragasvarRepository);

        filter.setChangedFrom(filter.getChangedFrom().minusDays(3));

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);

        //We should only have one that matches the filter
        Assert.assertTrue(fsList.size() == 1);
        //The SenasteHandelse should be set automatically from the SvarsDatum.
//        Assert.assertTrue(fsList.get(0).getSenasteHandelseDatum().equals(changeDateFrom));
        Assert.assertTrue(fsList.get(0).getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));
        fragasvarRepository.deleteAll();
    }
    @Test
    public void testFilterVidarebefordrad() {
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVidarebefordrad(true);
        FragaSvarTestUtil.populateFragaSvar(filter, 1, fragasvarRepository);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        Assert.assertTrue(fsList.size() == 1);
        Assert.assertTrue(fsList.get(0).getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));
        fragasvarRepository.deleteAll();
    }
    @Test
    public void testFilterWaitingForReplyFromCare() {
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVantarPa(VantarPa.SVAR_FRAN_VARDEN);
        FragaSvarTestUtil.populateFragaSvar(filter, 1,fragasvarRepository);

        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        Assert.assertTrue(fsList.size() == 1);
        Assert.assertTrue(fsList.get(0).getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));
        fragasvarRepository.deleteAll();
    }

    @Test
    public void testFilterWaitingForReplyFromFK() {
        //Add correct FragaSvar into repo
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.PENDING_EXTERNAL_ACTION,Amne.OVRIGT,"WC",FragaSvarTestUtil.visa_fraga,"HSA-ID",new LocalDateTime(2013,10,01,0,0),false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.PENDING_EXTERNAL_ACTION,Amne.ARBETSTIDSFORLAGGNING,"WC",FragaSvarTestUtil.visa_fraga,"HSA-ID",new LocalDateTime(2013,10,02,0,0),false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.PENDING_EXTERNAL_ACTION,Amne.AVSTAMNINGSMOTE,"WC",FragaSvarTestUtil.visa_fraga,"HSA-ID",new LocalDateTime(2013,10,03,0,0),false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.PENDING_EXTERNAL_ACTION,Amne.KONTAKT,"WC",FragaSvarTestUtil.visa_fraga,"HSA-ID",new LocalDateTime(2013,10,04,0,0),false));

        //Add incorrect data into repo. Create filter to more easily create them with util function.
        FragaSvarFilter tempfilter = new FragaSvarFilter();
        tempfilter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);
        tempfilter.setQuestionFromFK(true);
        tempfilter.setVidarebefordrad(true);
        FragaSvarTestUtil.populateFragaSvar(tempfilter, 5, fragasvarRepository);

        //Make the filter search
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVantarPa(VantarPa.SVAR_FRAN_FK);


        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        Assert.assertTrue(fsList.size() == 4);
        for (FragaSvar fs:fsList){
            Assert.assertTrue(fs.getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));

        }
        fragasvarRepository.deleteAll();
    }

    @Test
    public void testFilterMarkAsHandled() {
        //Add correct FragaSvar into repo
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.PENDING_INTERNAL_ACTION,Amne.PAMINNELSE,"FK",FragaSvarTestUtil.visa_fraga,"HSA-ID",null,false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.PENDING_INTERNAL_ACTION,Amne.MAKULERING_AV_LAKARINTYG,"FK",FragaSvarTestUtil.visa_fraga,"HSA-ID",null,false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.ANSWERED,Amne.KONTAKT,"WC",FragaSvarTestUtil.visa_fraga,"HSA-ID",null,false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.ANSWERED,Amne.KONTAKT,"FK",FragaSvarTestUtil.visa_fraga,"HSA-ID",null,false));

        //Add incorrect data into repo. Create filter to more easily create them with util function.
        FragaSvarFilter tempfilter = new FragaSvarFilter();
        tempfilter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);
        tempfilter.setQuestionFromFK(true);
        tempfilter.setVidarebefordrad(true);
        FragaSvarTestUtil.populateFragaSvar(tempfilter,5,fragasvarRepository);

        //Make the filter search
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVantarPa(VantarPa.MARKERA_SOM_HANTERAD);


        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        System.out.println("LISTA :" + fsList.size());
        Assert.assertTrue(fsList.size() == 4);
        for (FragaSvar fs:fsList){
            Assert.assertTrue(fs.getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));

        }
        fragasvarRepository.deleteAll();
    }


    @Test
    public void testFilterVantaPaKomplettering() {
        //Add correct FragaSvar into repo
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.PENDING_INTERNAL_ACTION,Amne.KOMPLETTERING_AV_LAKARINTYG,"FK",FragaSvarTestUtil.visa_fraga,"HSA-ID",null,false));

        //Add incorrect data into repo. Create filter to more easily create them with util function.
        FragaSvarFilter tempfilter = new FragaSvarFilter();
        tempfilter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);
        tempfilter.setQuestionFromFK(true);
        tempfilter.setVidarebefordrad(true);
        FragaSvarTestUtil.populateFragaSvar(tempfilter,5,fragasvarRepository);

        //Make the filter search
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVantarPa(VantarPa.KOMPLETTERING_FRAN_VARDEN);


        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        System.out.println("LISTA :" + fsList.size());
        Assert.assertTrue(fsList.size() == 1);
        for (FragaSvar fs:fsList){
            Assert.assertTrue(fs.getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));

        }
        fragasvarRepository.deleteAll();
    }


    @Test
    public void testFilterOHanterade() {
        //Add correct FragaSvar into repo
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarSvar(FragaSvarTestUtil.ENHET_1_ID,Status.PENDING_EXTERNAL_ACTION,Amne.OVRIGT,"WC",FragaSvarTestUtil.visa_fraga,"HSA-ID",null,null,false,"SVAR"));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarSvar(FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_EXTERNAL_ACTION, Amne.ARBETSTIDSFORLAGGNING, "WC", FragaSvarTestUtil.visa_fraga, "HSA-ID", null, null, false, "SVAR"));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarSvar(FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_EXTERNAL_ACTION, Amne.AVSTAMNINGSMOTE, "WC", FragaSvarTestUtil.visa_fraga, "HSA-ID", null, null, false, "SVAR"));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarSvar(FragaSvarTestUtil.ENHET_1_ID, Status.PENDING_EXTERNAL_ACTION, Amne.KONTAKT, "WC", FragaSvarTestUtil.visa_fraga, "HSA-ID", null, null, false, "SVAR"));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.PENDING_INTERNAL_ACTION,Amne.PAMINNELSE,"FK",FragaSvarTestUtil.visa_fraga,"HSA-ID",null,false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.PENDING_INTERNAL_ACTION,Amne.MAKULERING_AV_LAKARINTYG,"FK",FragaSvarTestUtil.visa_fraga,"HSA-ID",null,false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.ANSWERED,Amne.KONTAKT,"WC",FragaSvarTestUtil.visa_fraga,"HSA-ID",null,false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.ANSWERED,Amne.KONTAKT,"FK",FragaSvarTestUtil.visa_fraga,"HSA-ID",null,false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.PENDING_INTERNAL_ACTION,Amne.KOMPLETTERING_AV_LAKARINTYG,"FK",FragaSvarTestUtil.visa_fraga,"HSA-ID",null,false));

        //Add incorrect data into repo. Create filter to more easily create them with util function.
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.CLOSED,Amne.KONTAKT,"WC","FEL","HSA-ID",null,false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.CLOSED,Amne.KONTAKT,"FK","FEL","HSA-ID",null,false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.CLOSED,Amne.KOMPLETTERING_AV_LAKARINTYG,"FK","FEL","HSA-ID",null,false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.CLOSED,Amne.OVRIGT,"WC","FEL","HSA-ID",null,false));
        fragasvarRepository.save(FragaSvarTestUtil.buildFragaSvarFraga(FragaSvarTestUtil.ENHET_1_ID,Status.CLOSED,Amne.ARBETSTIDSFORLAGGNING,"WC","FEL","HSA-ID",null,false));


        //Make the filter search
        FragaSvarFilter filter = new FragaSvarFilter();
        filter.setEnhetsId(FragaSvarTestUtil.ENHET_1_ID);
        filter.setVantarPa(VantarPa.ALLA_OHANTERADE);


        List<FragaSvar> fsList = fragasvarRepository.filterFragaSvar(filter);
        System.out.println("LISTA :" + fsList.size());
        Assert.assertTrue(fsList.size() == 9);
        for (FragaSvar fs:fsList){
            Assert.assertTrue(fs.getFrageText().equalsIgnoreCase(FragaSvarTestUtil.visa_fraga));

        }
        fragasvarRepository.deleteAll();
    }
}
