package se.inera.webcert.spec.api

import se.inera.intyg.common.specifications.spec.util.QueryHelper
import se.inera.webcert.spec.util.RestClientFixture

/**
 * @author andreaskaltenbach
 */
class Mail extends RestClientFixture {

    public List<Object> query() {
        def restClient = createRestClient("${baseUrl}services/")
        def mails = restClient.get(path: "mail-stub/mails").data
        QueryHelper.asList(mails)
    }

}
