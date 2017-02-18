package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import gate.Annotation;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rich on 17/02/17.
 */
public class DocUtils {

    public Object convertDocToJSON(gate.Document doc) throws IOException {
        Map<String, Collection<Annotation>> gateMap = new HashMap<>();

        gateMap.put("bioyodie",doc.getAnnotations("Bio"));
        Object result = gate.corpora.DocumentJsonUtils.toJson(doc, gateMap);


        return result;
    }

}
