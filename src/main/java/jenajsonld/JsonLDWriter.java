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

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.writer.WriterDatasetRIOTBase ;

import com.fasterxml.jackson.core.JsonGenerationException ;
import com.fasterxml.jackson.databind.JsonMappingException ;
import com.github.jsonldjava.core.JSONLD ;
import com.github.jsonldjava.core.JSONLDProcessingError ;
import com.github.jsonldjava.utils.JSONUtils ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.util.Context ;

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

    private void serialize(Writer writer, DatasetGraph dataset, PrefixMap prefixMap, String baseURI)
    {
        try {
            Object obj = JSONLD.fromRDF(dataset, new JenaRDF2JSONLD()) ;

//            // From context
//            Options opts = new Options();
//            opts.addBlankNodeIDs = false ;
//            opts.useRdfType = true ;
//            opts.useNativeTypes = true ;
//            opts.skipExpansion = true ;
//            
//            // Expansion.
//            obj = JSONLD.expand(obj, opts);
//
//            // TODO: Framing
//            // TODO: Simplication
//
//            //output = JSONLD.frame(out, (Map<String, Object>)??, opts) ;
//            
//            obj = JSONLD.simplify(obj, opts);
            
            if ( isPretty() )
                JSONUtils.writePrettyPrint(writer, obj);
            else
                JSONUtils.write(writer, obj);
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
}