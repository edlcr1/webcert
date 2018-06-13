package se.inera.intyg.webcert.web.service.underskrift;

import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;

public interface UnderskriftService {
    SignaturBiljett startSigningProcess(String intygsId, String intygsTyp, long version);

    SignaturBiljett fakeSignature(String intygsId, String intygsTyp, long version, String ticketId);
}
