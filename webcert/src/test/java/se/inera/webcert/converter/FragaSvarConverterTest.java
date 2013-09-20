package se.inera.webcert.converter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import se.inera.certificate.integration.json.CustomObjectMapper;
import se.inera.webcert.persistence.FragaSvar;
import se.inera.webcert.receivemedicalcertificatequestionsponder.v1.QuestionFromFkType;

/**
 * @author andreaskaltenbach
 */
public class FragaSvarConverterTest {

    private QuestionFromFkType question() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(QuestionFromFkType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("FragaSvarConverterTest/question.xml").getInputStream()),
                QuestionFromFkType.class).getValue();
    }

    private void compareObjectWithReferenceFile(Object object, String fileName) throws IOException {
        ObjectMapper objectMapper = new CustomObjectMapper();
        JsonNode tree = objectMapper.valueToTree(object);
        JsonNode expectedTree = objectMapper.readTree(new ClassPathResource(fileName).getInputStream());
        assertEquals("JSON does not match expectation. Resulting JSON is \n" + tree.toString() + "\n", expectedTree,
                tree);
    }

    @Test
    public void testConvertQuestion() throws Exception {

        FragaSvar fragaSvar = new FragaSvarConverter().convert(question());

        compareObjectWithReferenceFile(fragaSvar, "FragaSvarConverterTest/question.json");

    }

}
