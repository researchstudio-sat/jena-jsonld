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
import java.util.Iterator ;
import java.util.Map ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.out.NodeToLabel ;
import org.apache.jena.riot.system.PrefixMap ;
import riot.RDFFormat ;
import riot.writer.WriterDatasetRIOTBase ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.util.Context ;

import de.dfki.km.json.JSONUtils ;
import de.dfki.km.json.jsonld.JSONLD ;
import de.dfki.km.json.jsonld.JSONLDProcessingError ;
import de.dfki.km.json.jsonld.JSONLDSerializer ;

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
        String str = serialize(dataset, prefixMap, baseURI) ;
        try { out.write(str) ; out.flush() ; }
        catch (IOException e) { IO.exception(e) ; }
    }
    
    private boolean isPretty() { return RDFFormat.wvPretty.equals(format.getVariant()) ; }

    @Override
    public void write(OutputStream out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context)
    {
        Writer w = new OutputStreamWriter(out, Chars.charsetUTF8) ;
        write(w, dataset, prefixMap, baseURI, context) ;
        IO.flush(w) ;
    }
    
    private String serialize(DatasetGraph dataset, PrefixMap prefixMap, String baseURI)
    {
        JSONLDSerializer serializer = new DatasetJSONLDSerializer() ;
        try
        {
            Object obj = JSONLD.fromRDF(dataset, serializer);
            if ( isPretty() )
                return JSONUtils.toPrettyString(obj) ;
            else
                // Flat.
                return JSONUtils.toString(obj) ;
        } catch (JSONLDProcessingError e)
        {
            e.printStackTrace();
            return null ;
        }
    }

    static class DatasetJSONLDSerializer extends JSONLDSerializer
    {
        @Override
        public void parse(Object object) throws JSONLDProcessingError
        {
            if ( ! ( object instanceof DatasetGraph ) )
            {
                Log.fatal("JsonLDWriter", "Unrecognized: "+object.getClass()) ;
                return ;
            }

            DatasetGraph dsg = (DatasetGraph)object ;
            // add the prefixes to the context
            Map<String, String> nsPrefixMap = dsg.getDefaultGraph().getPrefixMapping().getNsPrefixMap() ;
            for (String prefix : nsPrefixMap.keySet()) {
                setPrefix(nsPrefixMap.get(prefix), prefix);
            }

            Iterator<Quad> iter = dsg.find() ;
            for ( ; iter.hasNext() ; )
            {
                Quad quad = iter.next();
                importQuad(quad) ;
            }
        }

        public void importQuad(Quad quad) {
            String sStr = resource2String(quad.getSubject()) ;
            String pStr = resource2String(quad.getPredicate()) ;
            String gStr = (quad.isDefaultGraph() ? "" : resource2String(quad.getGraph())) ;
            if ( quad.getObject().isLiteral() )
            {
                Node o = quad.getObject() ;
                String value = o.getLiteralLexicalForm() ;
                String language = null ;
                String dt = null ;
                if ( o.getLiteralLanguage().equals("") )
                    dt = o.getLiteralDatatypeURI() ;
                else
                    language = o.getLiteralLanguage() ;
                triple(sStr, pStr, value, dt, language, gStr) ;
            }
            else
            {  
                String oStr = resource2String(quad.getObject()) ;
                triple(sStr, pStr, oStr, gStr) ;

            }
        }

        NodeToLabel labels = NodeToLabel.createScopeByDocument() ;
        String resource2String(Node node)
        {
            if ( node.isURI()) return node.getURI() ;
            if ( node.isBlank()) return labels.get(null, node) ;
            throw new IllegalStateException() ; 
        }
    }
}