/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package sdb;

import java.util.List;

import sdb.cmd.CmdArgsDB;
import arq.cmd.CmdException;
import arq.cmdline.ArgDecl;
import arq.cmdline.ModQueryIn;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.query.engine2.op.Op;
import com.hp.hpl.jena.query.util.Utils;
import com.hp.hpl.jena.sdb.core.compiler.OpSQL;
import com.hp.hpl.jena.sdb.engine.QueryEngineQuadSDB;
import com.hp.hpl.jena.sdb.sql.JDBC;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.sdb.store.Store;
import com.hp.hpl.jena.sdb.store.StoreDesc;
import com.hp.hpl.jena.sdb.util.PrintSDB;


/**
 * Compile and print the SQL for a SPARQL query.
 * @author Andy Seaborne
 * @version $Id: sdbprint.java,v 1.12 2006/04/24 17:31:26 andy_seaborne Exp $
 */

public class sdbprint extends CmdArgsDB
{
    LayoutType layoutDefault = LayoutType.LayoutTripleNodes ;

    static final String divider = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -" ;
    
    ModQueryIn modQuery = new ModQueryIn() ;
    ArgDecl argDeclPrintSQL     = new ArgDecl(ArgDecl.NoValue, "sql") ;
    ArgDecl argDeclPrint = new ArgDecl(ArgDecl.HasValue, "print") ;

    boolean printQuery = false ;

    boolean printPrefix = false ;

    boolean printOp = false ;

    boolean printSqlNode = false ;

    boolean printSQL = false ;
    public static void main (String [] argv)
    {
        new sdbprint(argv).mainAndExit() ;
    }
    
    protected sdbprint(String[] args)
    {
        super(args);
        super.addModule(modQuery) ;
        super.getUsage().startCategory("SQL") ;
        super.add(argDeclPrintSQL, "--sql", "Print SQL") ;
        super.add(argDeclPrint, "--print=", "Print any of query, prefix, op, sqlnode, SQL") ;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void processModulesAndArgs()
    {
        // Force the connection to be a null one.
        // Known to be called after arg module initialization.
        StoreDesc storeDesc = getModStore().getStoreDesc() ;
        if ( storeDesc.layout != LayoutType.LayoutRDB )
        {
            // Only fake the conenction if not ModelRDB
            // else we need a live conenction (currently)
            storeDesc.connDesc.jdbcURL = JDBC.jdbcNone ;
            storeDesc.connDesc.type = "none" ;
        }
        if ( storeDesc.layout == null )
            storeDesc.layout = layoutDefault ;
        
        printSQL = contains(argDeclPrintSQL) ;
        @SuppressWarnings("unchecked")
        List<String> strList = (List<String>)getValues(argDeclPrint) ;
        for ( String arg : strList )
        {
            if ( arg.equalsIgnoreCase("query"))         { printQuery = true ; }
            else if ( arg.equalsIgnoreCase("prefix"))   { printPrefix = true ; }
            else if ( arg.equalsIgnoreCase("Op"))       { printOp = true ; }
            else if ( arg.equalsIgnoreCase("SqlNode"))  { printSqlNode = true ; }
            else if ( arg.equalsIgnoreCase("sql"))      { printSQL = true ; }
            else
                throw new CmdException("Not a recognized print form: "+arg) ;
        }
    }

    @Override
    protected void execCmd(List<String> positionalArgs)
    {
        Query query = modQuery.getQuery() ;
        Store store = getModStore().getStore() ; 

        compilePrint(store, query) ;
  }

    boolean needDivider = false ;
    private void divider()
    {
        if ( needDivider ) System.out.println(divider) ;
        needDivider = true ;
    }
    
    private void compilePrint(Store store, Query query)
    {
        if ( !printQuery && ! printPrefix && ! printOp && ! printSqlNode && ! printSQL )
            printOp = true ;
        
        if ( isVerbose() )
        {
            //printQuery = true ;
            printPrefix = true ;
            printOp = true ;
            //printSqlNode = true ;
            printSQL = true ;
        }
        
        if ( printQuery )
        {
            divider() ;
            query.serialize(System.out, Syntax.syntaxARQ) ;
        }
        
        if ( printPrefix )
        {
            divider() ;
            query.serialize(System.out, Syntax.syntaxPrefix) ;
        }

        QueryEngineQuadSDB qe = new QueryEngineQuadSDB(store, query) ;
        Op op = qe.getOp() ;

        if ( printOp )
        {
            divider() ;
            PrintSDB.print(op) ;
            // No newline.
        }

        if ( printSqlNode )
        {
            if ( op instanceof OpSQL )
            {
                // Fix - print all SqlNode stuff by walk.
                divider() ;
                PrintSDB.print(((OpSQL)op).getSqlNode()) ;
            }
            else
                System.err.println("Top node is not an OpSQL") ;
        }
        
        if ( printSQL )
        {
            divider() ;
            PrintSDB.printSQL(op) ;
        }
    }

    
    
    @Override
    protected String getSummary()
    {
        return "Usage: [--layout schemaName] [--query URL | string ] " ;
    }

    @Override
    protected String getCommandName() { return Utils.className(this) ; }
}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */