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

import java.io.IOException ;
import java.io.OutputStream ;
import java.io.OutputStreamWriter ;
import java.io.Writer ;
import java.util.* ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.writer.WriterDatasetRIOTBase ;

import com.fasterxml.jackson.core.JsonGenerationException ;
import com.fasterxml.jackson.databind.JsonMappingException ;
import com.github.jsonldjava.core.JSONLD ;
import com.github.jsonldjava.core.JSONLDProcessingError ;
import com.github.jsonldjava.core.Options ;
import com.github.jsonldjava.core.RDFDatasetUtils ;
import com.github.jsonldjava.utils.JSONUtils ;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.vocabulary.RDF ;

class JsonLDWriter extends WriterDatasetRIOTBase
{
    private final RDFFormat format ;
    public JsonLDWriter(RDFFormat syntaxForm)
    {
        format = syntaxForm ;
    }

    @Override
    public Lang getLang()
    {
        return format.getLang() ;
    }

    @Override
    public void write(Writer out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context)
    {
        serialize(out, dataset, prefixMap, baseURI) ;
    }

    private boolean isPretty() { return RDFFormat.PRETTY.equals(format.getVariant()) ; }

    @Override
    public void write(OutputStream out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context)
    {
        Writer w = new OutputStreamWriter(out, Chars.charsetUTF8) ;
        write(w, dataset, prefixMap, baseURI, context) ;
        IO.flush(w) ;
    }

    private void serialize(Writer out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI)
    {
        try {
            Object obj = JSONLD.fromRDF(dataset, new THING()) ;
            // Options.
            //JSONUtils.write(out, obj) ;
            JSONUtils.writePrettyPrint(out, obj);
//            } else {
//            
//            }
        } catch (JSONLDProcessingError e) {
            e.printStackTrace();
        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class THING implements com.github.jsonldjava.core.RDFParser {

        @Override
        public Map<String, Object> parse(Object object) throws JSONLDProcessingError {
            Map<String,Object> result = RDFDatasetUtils.getInitialRDFDatasetResult();
            if ( object instanceof DatasetGraph )
            {
                DatasetGraph dsg = (DatasetGraph)object ;

                if ( ! result.containsKey("@default") )
                    result.put("@default", new ArrayList<Object>()) ;
                List<Object> triples =  (List<Object>)result.get("@default") ;
                Graph graph = dsg.getDefaultGraph() ;
                Iterator<Triple> iter = graph.find(null, null, null) ;
                for ( ; iter.hasNext() ; )
                {
                    Triple t = iter.next() ;
                    Map<String, Object> tx = encode(t) ;
                    triples.add(tx) ;
                }
            }                
            else
                Log.warn(THING.class, "unknown") ;
            return result ;
        } }

    private Map<String, Object> encode(Triple t) {
        Map<String, Object> map = new HashMap<String, Object>() ;
        encode(map, "subject", t.getSubject()) ;
        encode(map, "predicate", t.getPredicate()) ;
        encode(map, "object", t.getObject()) ;
        return map ;
    }

    private void encode(Map<String, Object> map, String string, Node node)
    {
        map.put(string, encode(node)) ;
    }

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
            map.put("value", n.getBlankNodeLabel()) ;
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
        Log.warn(THING.class, "encode miss") ;
        return null ;
    }
}