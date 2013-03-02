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
import java.io.InputStream ;

import com.hp.hpl.jena.datatypes.RDFDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.util.Context ;

import de.dfki.km.json.JSONUtils ;
import de.dfki.km.json.jsonld.JSONLD ;
import de.dfki.km.json.jsonld.JSONLDProcessingError ;
import de.dfki.km.json.jsonld.JSONLDTripleCallback ;

import org.apache.jena.atlas.web.ContentType ;
import org.apache.jena.riot.ReaderRIOT ;
import org.apache.jena.riot.lang.LabelToNode ;
import org.apache.jena.riot.system.StreamRDF ;

public class JsonLDReader implements ReaderRIOT
{
    @Override
    public void read(InputStream in, String baseURI, ContentType ct, final StreamRDF output, Context context)
    {
        try
        {
            Object jsonObject = JSONUtils.fromInputStream(in);
            JSONLDTripleCallback callback = new JSONLDTripleCallback() {

                @Override
                public void triple(String s, String p, String o, String graph)
                {
                    Node js = createURI(s) ;
                    Node jp = createURI(p) ;
                    Node jo = createURI(o) ;
                    Triple t = Triple.create(js, jp, jo) ;
                    output.triple(t) ;
                }

                @Override
                public void triple(String s, String p, String value, String datatype, String language, String graph)
                {
                    Node js = createURI(s) ;
                    Node jp = createURI(p) ;
                    Node jo = createLiteral(value,  datatype, language) ;
                    Triple t = Triple.create(js, jp, jo) ;
                    output.triple(t) ;
                }} ;

                JSONLD.toRDF(jsonObject, callback) ;
        } catch (IOException e)
        {
            e.printStackTrace();
        } catch (JSONLDProcessingError e)
        {
            e.printStackTrace();
        }
    }
    
    private LabelToNode labels =  LabelToNode.createScopeByDocument() ;
    
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

