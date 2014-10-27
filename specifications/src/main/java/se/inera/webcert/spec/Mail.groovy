package se.inera.webcert.spec

import se.inera.webcert.spec.util.QueryHelper
import se.inera.webcert.spec.util.RestClientFixture

/**
 * @author andreaskaltenbach
 */
class Mail extends RestClientFixture {

    public List<Object> query() {
        def restClient = createRestClient(baseUrl)
        def mails = restClient.get(path: "mail-stub/mails").data
        QueryHelper.asList(mails)
    }

}
