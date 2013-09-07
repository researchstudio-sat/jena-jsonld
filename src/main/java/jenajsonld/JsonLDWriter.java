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
import java.util.LinkedHashMap ;
import java.util.Map ;
import java.util.Map.Entry ;

import org.apache.jena.atlas.io.IO ;
import org.apache.jena.atlas.lib.Chars ;
import org.apache.jena.iri.IRI ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.RDFFormat ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.writer.WriterDatasetRIOTBase ;

import com.fasterxml.jackson.core.JsonGenerationException ;
import com.fasterxml.jackson.databind.JsonMappingException ;
import com.github.jsonldjava.core.JSONLD ;
import com.github.jsonldjava.core.JSONLDProcessingError ;
import com.github.jsonldjava.core.Options ;
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
        
        //Map<String, String> pmap = prefixMap.getMappingCopyStr() ;
        Map<String, IRI> pmap = prefixMap.getMapping() ;
        Map<String, Object> pmap2 = new LinkedHashMap<String, Object>();
        for ( Entry<String, IRI> e : pmap.entrySet()) {
            pmap2.put(e.getKey(),  e.getValue().toString()) ;
        }
        
        try {
            Object obj = JSONLD.fromRDF(dataset, new JenaRDF2JSONLD()) ;

            // From context
            Options opts = new Options();
            
            //opts.optimizeCtx = pmap2; // ?? Does not appear to be used by jsonld-java  
            opts.addBlankNodeIDs = false ;
            opts.useRdfType = true ;
            opts.useNativeTypes = true ;
            opts.skipExpansion = false ;
            opts.compactArrays = true ;
            opts.keepFreeFloatingNodes = false ;
            
            //AT A MINIMUM
            if ( true )
                obj = JSONLD.simplify(obj, opts);
            else {
                // Unclear as to the way to set better printing.
                if ( false )
                    obj = JSONLD.compact(obj, pmap2) ;
                
                if ( false )
                    obj = JSONLD.expand(obj, opts);
                Map<String, Object> inframe = new LinkedHashMap<String, Object>() ;
                if ( false )
                    obj = JSONLD.frame(obj, inframe, opts);

                if ( false )
                    // This seems to undo work done in earlier steps.
                    obj = JSONLD.simplify(obj, opts);
            }
            
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