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

import java.util.* ;

import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.out.NodeToLabel ;
import org.apache.jena.riot.system.SyntaxLabels ;

import com.github.jsonldjava.core.JSONLDProcessingError ;
import com.github.jsonldjava.core.RDFDatasetUtils ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.vocabulary.RDF ;

// From RDF to JSOnLD java structure.
class JenaRDF2JSONLD implements com.github.jsonldjava.core.RDFParser {

    @Override
    public Map<String, Object> parse(Object object) throws JSONLDProcessingError {
        Map<String,Object> result = RDFDatasetUtils.getInitialRDFDatasetResult();
        if ( object instanceof DatasetGraph )
        {
            DatasetGraph dsg = (DatasetGraph)object ;

            if ( ! result.containsKey("@default") )
                result.put("@default", new ArrayList<Object>()) ;
            @SuppressWarnings("unchecked")
            List<Object> triples =  (List<Object>)result.get("@default") ;
            Graph graph = dsg.getDefaultGraph() ;
            parse(graph, triples) ;

            Iterator<Node> graphNames = dsg.listGraphNodes() ;
            for ( ; graphNames.hasNext() ; )
            {
                List<Object> data = new ArrayList<Object>() ;
                Node gn = graphNames.next() ;
                Graph g = dsg.getGraph(gn) ;
                parse(g, data) ;
                result.put(gn.getURI(), data) ;
            }
        }                
        else
            Log.warn(JenaRDF2JSONLD.class, "unknown") ;
        return result ;
    }

    public void parse(Graph graph, List<Object> triples) {
        Iterator<Triple> iter = graph.find(null, null, null) ;

        for ( ; iter.hasNext() ; )
        {
            Triple t = iter.next() ;
            //System.out.println("Serializing "+FmtUtils.stringForTriple(t)) ;
            Map<String, Object> tx = encode(t) ;
            triples.add(tx) ;
        }
    }

    private Map<String, Object> encode(Triple t) {
        Map<String, Object> map = new HashMap<String, Object>() ;
        encode(map, "subject", t.getSubject()) ;
        encode(map, "predicate", t.getPredicate()) ;
        encode(map, "object", t.getObject()) ;
        //System.out.println(map) ;
        return map ;
    }

    private void encode(Map<String, Object> map, String string, Node node)
    {
        map.put(string, encode(node)) ;
    }

    private NodeToLabel labels = SyntaxLabels.createNodeToLabel() ;

    private Map<String, Object> encode(Node n) {
        Map<String, Object> map = new HashMap<String, Object>() ;
        if ( n.isURI() )
        {
            map.put("type", JsonLDReader.IRI) ;
            map.put("value", n.getURI()) ;
            return map ;
        }
        if ( n.isBlank() )
        {
            map.put("type", JsonLDReader.BLANK_NODE) ;
            //String v = "_:"+n.getBlankNodeLabel() ; 
            String v = labels.get(null, n) ;
            map.put("value", v);
            return map ;    
        }
        if ( n.isLiteral() )
        {
            map.put("type", JsonLDReader.LITERAL) ;
            map.put("value", n.getLiteralLexicalForm()) ;
            String lang = n.getLiteralLanguage() ;
            String dt = n.getLiteralDatatypeURI() ;
            if (lang != null && lang.length()>0)
            {
                map.put("language", lang) ;
                map.put("datatype", RDF.getURI()+"langString") ;
                return map ;
            }
            if (dt == null )
            {
                map.put("datatype", XSDDatatype.XSDstring.getURI()) ;
                return map ;
            }
            map.put("datatype", dt) ;
            return map ;    
        }
        Log.warn(JenaRDF2JSONLD.class, "encode miss") ;
        return null ;
    }
}
