package se.inera.intyg.webcert.web.web.controller.moduleapi;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.Privilege;
import se.inera.intyg.infra.security.common.model.RequestOrigin;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.common.model.SekretessStatus;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.fragasvar.model.FragaSvar;
import se.inera.intyg.webcert.persistence.fragasvar.model.IntygsReferens;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.fragasvar.FragaSvarService;
import se.inera.intyg.webcert.web.service.patient.PatientDetailsResolver;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.web.controller.api.dto.FragaSvarView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by eriklupander on 2017-10-03.
 */
@RunWith(MockitoJUnitRunner.class)
public class FragaSvarModuleApiControllerTest {

    private static final String FK7263 = "fk7263";
    private static final String INTYG_ID = "abc-123";
    @Mock
    private PatientDetailsResolver patientDetailsResolver;

    @Mock
    private WebCertUserService webCertUserService;

    @Mock
    private FragaSvarService fragaSvarService;

    @InjectMocks
    private FragaSvarModuleApiController testee = new FragaSvarModuleApiController();

    @Before
    public void setup() {

        when(webCertUserService.getUser()).thenReturn(buildUser());

        List<FragaSvarView> fragaSvarViewList = buildFragaSvarViewList();
        when(fragaSvarService.getFragaSvar(INTYG_ID)).thenReturn(fragaSvarViewList);

        testee.setWebCertUserService(webCertUserService);
    }

    @Test
    public void testGetFragaSvarNotSekretessmarkerad() {
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.FALSE);
        List<FragaSvarView> fragaSvarViewList = testee.fragaSvarForIntyg(FK7263, INTYG_ID);
        assertNotNull(fragaSvarViewList);
        assertEquals(1, fragaSvarViewList.size());
    }

    @Test(expected = WebCertServiceException.class)
    public void testGetFragaSvarWithSekretessPatientForVardadminThrowsException() {
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.TRUE);
        testee.fragaSvarForIntyg(FK7263, INTYG_ID);
    }

    @Test(expected = WebCertServiceException.class)
    public void testGetFragaSvarWithPuFailsForVardadminThrowsException() {
        when(patientDetailsResolver.getSekretessStatus(any(Personnummer.class))).thenReturn(SekretessStatus.UNDEFINED);
        testee.fragaSvarForIntyg(FK7263, INTYG_ID);
    }

    private List<FragaSvarView> buildFragaSvarViewList() {
        List<FragaSvarView> fragaSvarViewList = new ArrayList<>();


        IntygsReferens intygsReferens = mock(IntygsReferens.class);
        when(intygsReferens.getPatientId()).thenReturn(new Personnummer("191212121212"));

        FragaSvar fs = mock(FragaSvar.class);
        when(fs.getIntygsReferens()).thenReturn(intygsReferens);

        FragaSvarView fsw = mock(FragaSvarView.class);
        when(fsw.getFragaSvar()).thenReturn(fs);
        fragaSvarViewList.add(fsw);
        return fragaSvarViewList;
    }

    private WebCertUser buildUser() {
        WebCertUser user = new WebCertUser();
        user.setAuthorities(new HashMap<>());

        user.getAuthorities().put(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG,
                createPrivilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG));
        user.setFeatures(ImmutableSet
                .of(WebcertFeature.HANTERA_FRAGOR.getName(),WebcertFeature.HANTERA_FRAGOR.getName() + "." + FK7263));
        user.setOrigin(WebCertUserOriginType.UTHOPP.name());
        return user;
    }

    protected Privilege createPrivilege(String privilege) {
        Privilege priv = new Privilege();
        priv.setName(privilege);
        RequestOrigin requestOrigin = new RequestOrigin();
        requestOrigin.setName(WebCertUserOriginType.UTHOPP.name());
        requestOrigin.setIntygstyper(Arrays.asList(FK7263));
        priv.setRequestOrigins(Arrays.asList(requestOrigin));
        priv.setIntygstyper(Arrays.asList(FK7263));
        return priv;
    }
}
