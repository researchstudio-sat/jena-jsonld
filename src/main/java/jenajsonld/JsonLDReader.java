/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jenajsonld;

import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.core.JSONLDProcessingError;
import com.github.jsonldjava.core.JSONLDTripleCallback;
import com.github.jsonldjava.core.RDFDataset;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import org.openjena.atlas.lib.InternalErrorException;
import org.openjena.riot.lang.LabelToNode;
import org.openjena.riot.lang.SinkQuadsToDataset;
import org.openjena.riot.system.SyntaxLabels;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class JsonLDReader
{


    public Dataset read(InputStream in, String baseURI)
    {
        Dataset dataset = new DatasetImpl(ModelFactory.createDefaultModel());
        final SinkQuadsToDataset sink = new SinkQuadsToDataset(dataset.asDatasetGraph());
        try
        {
            Object jsonObject = JSONUtils.fromInputStream(in);
            JSONLDTripleCallback callback = new JSONLDTripleCallback() {

                @Override
                //public Object call(Map<String, Object> dataset) {
                public Object call(RDFDataset dataset) {
                    for ( String gn : dataset.keySet() )
                    {
                        Object x = dataset.get(gn) ;
                        if ( "@default".equals(gn) )
                        {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> triples = (List<Map<String, Object>>)x ; 
                            for ( Map<String, Object> t : triples )
                            {
                                    Node s = createNode(t, "subject") ;
                                    Node p = createNode(t, "predicate") ;
                                    Node o = createNode(t, "object") ;
                                    Quad quad = Quad.create(null, s, p, o) ;
                                    sink.send(quad);
                            }
                        } else {
                            @SuppressWarnings("unchecked")
                            List<Map<String, Object>> quads = (List<Map<String, Object>>)x ;
                            Node g = Node.createURI(gn) ;    // Bnodes?
                            for ( Map<String, Object> q : quads )
                            {
                                    Node s = createNode(q, "subject") ;
                                    Node p = createNode(q, "predicate") ;
                                    Node o = createNode(q, "object") ;
                                    Quad quad = Quad.create(g, s, p, o) ;
                                    sink.send(quad);
                            }

                        }
                    }
                    return null ;
                }} ;

                JSONLD.toRDF(jsonObject, callback) ;
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (JSONLDProcessingError e)
        {
            e.printStackTrace();
        }
      return dataset;
    }
    
    private LabelToNode labels =  SyntaxLabels.createLabelToNode() ;
    
    public static String LITERAL = "literal";
    public static String BLANK_NODE = "blank node";
    public static String IRI = "IRI";
    
    private Node createNode(Map<String, Object> tripleMap, String key)
    {
        @SuppressWarnings("unchecked")
        Map<String, Object> x = (Map<String, Object>)(tripleMap.get(key)) ;
        return createNode(x) ;
    }
    // See RDFParser
    private Node createNode(Map<String, Object> map)
    {
        String type = (String)map.get("type") ;
        String lex = (String)map.get("value") ;
        if ( type.equals(IRI) )
          return Node.createURI(lex) ;
        else if ( type.equals(BLANK_NODE) )
            return labels.get(null,  lex) ;
        else if ( type.equals(LITERAL) )
        {
            String lang = (String)map.get("language") ;
            String datatype = (String)map.get("datatype") ;
            if ( lang == null && datatype == null )
                return Node.createLiteral(lex) ;
            if ( lang != null )
                return Node.createLiteral(lex, lang, null) ;
            RDFDatatype dt = Node.getType(datatype) ;
            return Node.createLiteral(lex, dt) ;
        }
        else
            throw new InternalErrorException("Node is not a IRI, bNode or a literal: "+type) ;
//        /*
//     *  "value" : The value of the node.
//     *            "subject" can be an IRI or blank node id.
//     *            "predicate" should only ever be an IRI
//     *            "object" can be and IRI or blank node id, or a literal value (represented as a string) 
//     *  "type" : "IRI" if the value is an IRI or "blank node" if the value is a blank node.
//     *           "object" can also be "literal" in the case of literals.
//     * The value of "object" can  also contain the following optional key-value pairs:
//     *  "language" : the language value of a string literal
//     *  "datatype" : the datatype of the literal. (if not set will default to XSD:string, if set to null, null will be used).         */
//        System.out.println(map.get("value")) ;
//        System.out.println(map.get("type")) ;
//        System.out.println(map.get("language")) ;
//        System.out.println(map.get("datatype")) ;
//        return null ;
    }
    
    private Node createURI(String str)
    {
        if ( str.startsWith("_:") )
            return labels.get(null, str) ;
        else
            return Node.createURI(str) ;
    }
    
    private Node createLiteral(String lex, String datatype, String lang)
    {
        if ( lang == null && datatype == null )
            return Node.createLiteral(lex) ;
        if ( lang != null )
            return Node.createLiteral(lex, lang, null) ;
        RDFDatatype dt = Node.getType(datatype) ;
        return Node.createLiteral(lex, dt) ;
    }

}

