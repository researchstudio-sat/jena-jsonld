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

package dev;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;

import jenajsonld.JenaJSONLD ;
import static jenajsonld.JenaJSONLD.JSONLD; 
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.RDFDataMgr ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;
import riot.RDFWriterMgr ;
import riot.system.IO_Jena2 ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.impl.IO_Ctl ;

public class MainJsonLD
{
    static { Log.setLog4j() ; }
    private static Logger log = LoggerFactory.getLogger("JsonLD") ;
    
    public static void main(String[] args)
    {
        IO_Ctl.init() ;
        IO_Jena2.wireIntoJenaW() ;
        
        JenaJSONLD.init() ;
        
        rtRJR("D.ttl") ;
        rtRJR("data.jsonld") ;
        
        rtRJR2("D.ttl") ;
        rtRJR2("data.jsonld") ;
        System.out.println("DONE") ;
        System.exit(0) ;

        String filename = "data.jsonld" ;
        
//        StreamRDF stream = new PrintingStreamRDF(log) ;
//        RDFDataMgr.parse(stream, filename) ;
        
        Model model = RDFDataMgr.loadModel(filename) ;
        RDFWriterMgr.write(System.out, model, JSONLD) ;
        
        System.out.print("\n## -------------\n\n") ;
        
        model.write(System.out, "JSON-LD") ;
    }
    
    static void rtRJR(String filename)
    {
        System.out.println("## ---- : "+filename) ;
        Model model = RDFDataMgr.loadModel(filename) ;
        
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        RDFWriterMgr.write(out, model, JSONLD) ;
        ByteArrayInputStream r = new ByteArrayInputStream(out.toByteArray()) ;
        
        Model model2 = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(model2, r, null, JSONLD) ;
        if ( ! model.isIsomorphicWith(model2) ) 
            System.out.println("## ---- DIFFERENT") ;
    }
    
    static void rtRJR2(String filename)
    {
        rtRJR2(filename, JSONLD.getName()) ;
    }
    
    private static void rtRJR2(String filename, String formatName)
    {
        System.out.println("## ---- : "+filename) ;
        
        Model model = ModelFactory.createDefaultModel().read(filename) ;        
        
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        model.write(out, formatName) ;
        ByteArrayInputStream r = new ByteArrayInputStream(out.toByteArray()) ;
        
        Model m2 = ModelFactory.createDefaultModel().read(r, null, formatName) ;

        if ( ! model.isIsomorphicWith(m2) ) 
            System.out.println("## ---- DIFFERENT") ;
    }

}

