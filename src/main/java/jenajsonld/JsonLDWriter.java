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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.core.JSONLDProcessingError;
import com.github.jsonldjava.core.Options;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetImpl;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.vocabulary.RDF;
import org.openjena.atlas.iterator.Action;
import org.openjena.atlas.iterator.Iter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.Map.Entry;

public class JsonLDWriter {
    private final boolean pretty ;
    public JsonLDWriter(boolean pretty)
    {
        this.pretty = pretty;
    }

    public void write(OutputStream out, Graph graph, String baseURI) throws IOException
    {
      write(out, new DatasetImpl(ModelFactory.createModelForGraph(graph)).asDatasetGraph(), baseURI) ;
    }

    public void write(Writer out, Graph graph, String baseURI) throws IOException
    {
      write(out, new DatasetImpl(ModelFactory.createModelForGraph(graph)).asDatasetGraph(), baseURI) ;
    }

    public void write(OutputStream out, Model model, String baseURI) throws IOException
    {
      write(out, DatasetFactory.assemble(model).asDatasetGraph(), baseURI) ;
    }

    public void write(Writer out, Model model, String baseURI) throws IOException
    {
      write(out, DatasetFactory.assemble(model).asDatasetGraph(), baseURI) ;
    }

    public void write(OutputStream out, Dataset dataset, String baseURI) throws IOException
    {
      write(out, dataset.asDatasetGraph(), baseURI) ;
    }

    public void write(Writer out, Dataset dataset, String baseURI)
    {
      serialize(out, dataset.asDatasetGraph(), baseURI) ;
    }

    public void write(Writer out, DatasetGraph graph, String baseURI)
    {
        serialize(out, graph, baseURI) ;
    }

    private boolean isPretty() { return pretty;}

    public void write(OutputStream out, DatasetGraph graph, String baseURI) throws IOException
    {
      Writer w = null;
      w = new OutputStreamWriter(out, "UTF-8") ;
      write(w, graph, baseURI) ;
      w.flush();
    }

    private void serialize(Writer writer, DatasetGraph graph,String baseURI)
    {
        final Map<String, Object> ctx = new LinkedHashMap<String, Object>();
        addProperties(ctx, graph) ;
        addPrefixes(ctx, graph.getDefaultGraph().getPrefixMapping()) ;
        
        try {
            Object obj = JSONLD.fromRDF(graph, new JenaRDF2JSONLD()) ;
            Options opts = new Options();
            opts.graph =  false ;
            opts.addBlankNodeIDs = false ;
            opts.useRdfType = true ;
            opts.useNativeTypes = true ;
            opts.skipExpansion = false ;
            opts.compactArrays = true ;
            opts.keepFreeFloatingNodes = false ;
            Map<String, Object> localCtx = new HashMap<>() ;
            localCtx.put("@context", ctx);
            
            
            //AT A MINIMUM
            if ( false )
                obj = JSONLD.simplify(obj, opts);
            else
                // Unclear as to the way to set better printing.
                obj = JSONLD.compact(obj, localCtx) ;
            
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

    private static void addPrefixes(Map<String, Object> ctx, PrefixMapping prefixMap) {
        Map<String, String> pmap = prefixMap.getNsPrefixMap();
        for ( Entry<String, String> e : pmap.entrySet()) {
          String key = e.getKey();
          String value = e.getValue();
            if (key.trim().length() == 0){
              if  (value.trim().length() > 0) {
                //set default URI prefix
                ctx.put("base",e.getValue());
              } //ignore if the value is empty
            } else {
              ctx.put(e.getKey(),  e.getValue()) ;
            }
        }
    }

  /**
   * Adds context elements based on the data observed.
   * @param ctx
   * @param graph
   */
    private void addProperties(final Map<String, Object> ctx, DatasetGraph graph) {
        // Add some properties directly so it becomes "localname": ....
        final Set<String> dups = new HashSet<>() ;
        Action<Quad> x = new Action<Quad>() {
            @Override
            public void apply(Quad item) {
                Node p = item.getPredicate() ;
                Node o = item.getObject();
                if ( p.equals( RDF.type.asNode() ))
                    return ;
                String x = p.getLocalName() ;
                if ( dups.contains(x))
                    return ;
                
                if ( ctx.containsKey(x) ) {
                    // Check different URI
//                    pmap2.remove(x) ;
//                    dups.add(x) ;  
                } else if (o.isBlank() || o.isURI()) {
                    //add property as a property (the object is an IRI)
                    Map<String, Object> x2 = new LinkedHashMap<String, Object>();
                    x2.put("@id", p.getURI()) ;
                    x2.put("@type", "@id") ;
                    ctx.put(x, x2) ;
                } else if (o.isLiteral()) {
                    String literalDatatypeURI = o.getLiteralDatatypeURI();
                    if (literalDatatypeURI != null){
                      //add property as a typed attribute (the object is a typed literal)
                      Map<String, Object> x2 = new LinkedHashMap<String, Object>();
                      x2.put("@id", p.getURI()) ;
                      x2.put("@type", literalDatatypeURI) ;
                      ctx.put(x, x2) ;
                    } else {
                      //add property as an untyped attribute (the object is an untyped literal)
                      ctx.put(x,p.getURI());
                    }
                }
            }
        } ; 
        
        Iter.iter(graph.find()).apply(x);

    }
}