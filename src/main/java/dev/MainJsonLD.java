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

import static jenajsonld.JenaJSONLD.JSONLD ;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;

import jenajsonld.JenaJSONLD ;
import org.apache.jena.atlas.logging.Log ;
import org.apache.jena.riot.RDFDataMgr ;
import org.slf4j.Logger ;
import org.slf4j.LoggerFactory ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class MainJsonLD
{
    static { Log.setLog4j() ; }
    private static Logger log = LoggerFactory.getLogger("JsonLD") ;
    
    public static void main(String[] args)
    {
        JenaJSONLD.init() ;
        String DIR = "testing/RIOT/jsonld/" ; 
        
        Dataset ds = RDFDataMgr.loadDataset("D.trig") ;
        RDFDataMgr.write(System.out, ds, JSONLD) ;
    }
    
    static void rtRJR(String filename)
    {
        System.out.println("## ---- : "+filename) ;
        Dataset ds = RDFDataMgr.loadDataset(filename) ;
        
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        RDFDataMgr.write(out, ds, JSONLD) ;
        RDFDataMgr.write(System.out, ds, JSONLD) ;
        System.out.println() ;
        ByteArrayInputStream r = new ByteArrayInputStream(out.toByteArray()) ;
        
        Dataset ds2 = DatasetFactory.createMem() ;
        RDFDataMgr.read(ds2, r, null, JSONLD) ;
//        if ( ! model.isIsomorphicWith(model2) ) 
//            System.out.println("## ---- DIFFERENT") ;
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

