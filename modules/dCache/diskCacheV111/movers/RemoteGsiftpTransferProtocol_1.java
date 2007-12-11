// $Id: RemoteGsiftpTransferProtocol_1.java,v 1.12 2007-10-08 20:43:29 abaranov Exp $

/*
COPYRIGHT STATUS:
  Dec 1st 2001, Fermi National Accelerator Laboratory (FNAL) documents and
  software are sponsored by the U.S. Department of Energy under Contract No.
  DE-AC02-76CH03000. Therefore, the U.S. Government retains a  world-wide
  non-exclusive, royalty-free license to publish or reproduce these documents
  and software for U.S. Government purposes.  All documents and software
  available from this server are protected under the U.S. and Foreign
  Copyright Laws, and FNAL reserves all rights.


 Distribution of the software available from this server is free of
 charge subject to the user following the terms of the Fermitools
 Software Legal Information.

 Redistribution and/or modification of the software shall be accompanied
 by the Fermitools Software Legal Information  (including the copyright
 notice).

 The user is asked to feed back problems, benefits, and/or suggestions
 about the software to the Fermilab Software Providers.


 Neither the name of Fermilab, the  URA, nor the names of the contributors
 may be used to endorse or promote products derived from this software
 without specific prior written permission.



  DISCLAIMER OF LIABILITY (BSD):

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  "AS IS" AND ANY EXPRESS OR IMPLIED  WARRANTIES, INCLUDING, BUT NOT
  LIMITED TO, THE IMPLIED  WARRANTIES OF MERCHANTABILITY AND FITNESS
  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL FERMILAB,
  OR THE URA, OR THE U.S. DEPARTMENT of ENERGY, OR CONTRIBUTORS BE LIABLE
  FOR  ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
  OF SUBSTITUTE  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY  OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT  OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE  POSSIBILITY OF SUCH DAMAGE.


  Liabilities of the Government:

  This software is provided by URA, independent from its Prime Contract
  with the U.S. Department of Energy. URA is acting independently from
  the Government and in its own private capacity and is not acting on
  behalf of the U.S. Government, nor as its contractor nor its agent.
  Correspondingly, it is understood and agreed that the U.S. Government
  has no connection to this software and in no manner whatsoever shall
  be liable for nor assume any responsibility or obligation for any claim,
  cost, or damages arising out of or resulting from the use of the software
  available from this server.


  Export Control:

  All documents and software available from this server are subject to U.S.
  export control laws.  Anyone downloading information from this server is
  obligated to secure any necessary Government licenses before exporting
  documents or software obtained from this server.
 */

package diskCacheV111.movers;

import diskCacheV111.repository.SpaceMonitor;
import diskCacheV111.util.CacheException;
import diskCacheV111.util.Checksum;
import diskCacheV111.util.ChecksumFactory;
import diskCacheV111.util.PnfsHandler;
import diskCacheV111.util.PnfsId;
import diskCacheV111.vehicles.ProtocolInfo;
import diskCacheV111.vehicles.StorageInfo;
import diskCacheV111.vehicles.transferManager.RemoteGsiftpDelegateUserCredentialsMessage;
import diskCacheV111.vehicles.transferManager.RemoteGsiftpTransferProtocolInfo;
import dmg.cells.nucleus.CellAdapter;
import dmg.cells.nucleus.CellMessage;
import dmg.cells.nucleus.CellPath;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.dcache.srm.Logger;
import org.dcache.srm.util.GridftpClient.IDiskDataSourceSink;
import org.dcache.srm.util.GridftpClient;
import org.globus.ftp.Buffer;
import org.globus.gsi.gssapi.auth.Authorization;
import org.globus.gsi.gssapi.auth.AuthorizationException;
import org.globus.gsi.gssapi.net.GssSocket;
import org.globus.gsi.gssapi.net.impl.GSIGssSocket;
import org.globus.util.GlobusURL;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class RemoteGsiftpTransferProtocol_1
    implements MoverProtocol,ChecksumMover,DataBlocksRecipient
{
    public static final int READ   =  1;
    public static final int WRITE  =  2;
    public static final long SERVER_LIFE_SPAN= 60 * 5 * 1000; /* 5 minutes */

    private final CellAdapter cell;
    private RemoteGsiftpTransferProtocolInfo remoteGsiftpProtocolInfo;
    private long starttime;
    private long timeout_time;
    private PnfsId pnfsId;

    // checksum related variables
    // copied from DCapProtocol_3_nio

    private MessageDigest _transferMessageDigest = null;
    private Checksum _transferChecksum      = null;

    // if we receive extendedn mode blocks,
    // we still try to calculate the checksum
    // but if this fails
    // we can calculate the checksum
    // on the end file
    private long previousUpdateEndOffset = 0;
    private boolean recalculateOnFile=false;

    // the random access file we are wrinting to or reading from
    private RandomAccessFile raDiskFile;


    //
    // <init>(CellAdapter cell);
    //

    public RemoteGsiftpTransferProtocol_1(CellAdapter cell) {
        this.cell = cell;
    }

    private void say(String str){
        if (pnfsId != null) {
            str ="(RemoteGsiftpTransferProtocol_1 for "+
                pnfsId.toIdString()+") "+str;
        }
        cell.say(str);
    }

    private void esay(String str){
        if (pnfsId != null) {
            str ="(RemoteGsiftpTransferProtocol_1 for "+
                pnfsId.toIdString()+") "+str;
        }
        cell.esay(str);
    }

    private void esay(Throwable t) {
        cell.esay(t);
    }

    public void runIO(RandomAccessFile diskFile, ProtocolInfo protocol, StorageInfo storage, PnfsId pnfsId, SpaceMonitor spaceMonitor, int access) throws Exception {
        this.pnfsId = pnfsId;
        say("runIO()\n\tprotocol="+
            protocol+",\n\tStorageInfo="+storage+",\n\tPnfsId="+pnfsId+
            ",\n\taccess ="+(((access & MoverProtocol.WRITE) != 0)?"WRITE":"READ"));
        if (! (protocol instanceof RemoteGsiftpTransferProtocolInfo)) {
            throw new  CacheException("protocol info is not RemoteGsiftpransferProtocolInfo");
        }
        this.raDiskFile = diskFile;
        starttime = System.currentTimeMillis();

        remoteGsiftpProtocolInfo = (RemoteGsiftpTransferProtocolInfo) protocol;

        //remoteURL = remoteGsiftpProtocolInfo.getGsiftpUrl();

        CellPath cellpath = new CellPath(remoteGsiftpProtocolInfo.getGsiftpTranferManagerName(),
                                         remoteGsiftpProtocolInfo.getGsiftpTranferManagerDomain());
        say(" runIO() RemoteGsiftpTranferManager cellpath="+cellpath);

        ServerSocket ss= null;
        try {
            ss = new ServerSocket(0,1);
            //timeout after 5 minutes if credentials not delegated
            ss.setSoTimeout(5*60*1000);
        } catch (IOException ioe) {
            esay("exception while trying to create a server socket : "+ioe);
            throw ioe;
        }

        RemoteGsiftpDelegateUserCredentialsMessage cred_request =
            new RemoteGsiftpDelegateUserCredentialsMessage(remoteGsiftpProtocolInfo.getId(),
                                                           remoteGsiftpProtocolInfo.getSourceId(),
                                                           InetAddress.getLocalHost().getHostName(),
                                                           ss.getLocalPort(),
                                                           remoteGsiftpProtocolInfo.getRequestCredentialId());

        say(" runIO() created message");
        cell.sendMessage(new CellMessage(cellpath,cred_request));
        say("waiting for delegation connection");
        //timeout after 5 minutes if credentials not delegated
        Socket deleg_socket = ss.accept();
        say("connected");
        try {
            ss.close();
        } catch (IOException ioe) {
            esay("failed to close server socket");
            esay(ioe);
            // we still can continue, this is non-fatal
        }
        GSSCredential deleg_cred;
        try {
            GSSContext context = getServerContext();
            GSIGssSocket gsiSocket = new GSIGssSocket(deleg_socket, context);
            gsiSocket.setUseClientMode(false);
            gsiSocket.setAuthorization(new Authorization() {
                                           public void authorize(GSSContext context, String host) {
                                               //we might add some authorization here later
                                               //but in general we trust that the connection
                                               //came from a head node and user was authorized
                                               //already
                                           }
                                       }

                                      );
            gsiSocket.setWrapMode(GssSocket.SSL_MODE);
            gsiSocket.startHandshake();

            deleg_cred = context.getDelegCred();
            gsiSocket.close();
            /*
             *  the following code saves delegated credentials in a file
             *  this can be used for debugging the gsi problems
             *
             try
             {
             byte [] data = ((ExtendedGSSCredential)(deleg_cred)).export(
             ExtendedGSSCredential.IMPEXP_OPAQUE);
             String proxy_file = "/tmp/fnisd1.pool.proxy.pem";
             FileOutputStream out = new FileOutputStream(proxy_file);
             out.write(data);
             out.close();
             }catch (Exception e)
             {
             esay(e);
             }
            */
        } catch (Throwable t) {
            esay(t);
            // we do not propogate this exception since some exceptions
            // we catch are not serializable!!!
            throw new Exception(t.toString());
        }


        if (deleg_cred != null) {
            say("successfully received user credentials: "+deleg_cred.getName().toString());
        } else {
            throw new Exception("delegation request failed");
        }

        Logger logger =   new Logger() {
                public synchronized void log(String s) {
                    say(s);
                }
                public synchronized void elog(String s) {
                    esay(s);
                }
                public synchronized void elog(Throwable t) {
                    esay(t);
                }
            };
        GlobusURL url =  new GlobusURL(remoteGsiftpProtocolInfo.getGsiftpUrl());
        client = new GridftpClient(url.getHost(),
                                   url.getPort(), remoteGsiftpProtocolInfo.getTcpBufferSize(),
                                   deleg_cred,logger);
        client.setStreamsNum(remoteGsiftpProtocolInfo.getStreams_num());
        client.setTcpBufferSize(remoteGsiftpProtocolInfo.getTcpBufferSize());

        if ((access & MoverProtocol.WRITE) != 0) {
            gridFTPRead(
                        remoteGsiftpProtocolInfo,
                        storage,
                        pnfsId,
                        spaceMonitor,
                        deleg_cred);
        } else {
            gridFTPWrite(
                         remoteGsiftpProtocolInfo,
                         storage,
                         pnfsId,
                         spaceMonitor,
                         deleg_cred);
        }
        say(" runIO() done");
    }

    public long getLastTransferred() {
        if (client == null) {
            return 0;
        } else {
            return client.getLastTransferTime();
        }
    }

    private synchronized void setTimeoutTime(long t) {
        timeout_time = t;
    }
    private synchronized long  getTimeoutTime() {
        return timeout_time;
    }
    public void setAttribute(String name, Object attribute) {
    }
    public Object getAttribute(String name) {
        return null;
    }
    public long getBytesTransferred() {
        if (client == null) {
            return 0;
        } else {
            return client.getTransfered();
        }
    }

    public long getTransferTime() {
        return System.currentTimeMillis() -starttime;
    }

    public boolean wasChanged() {
        return client == null;
    }

    private GSSContext getServerContext() throws GSSException {
        return org.dcache.srm.security.SslGsiSocketFactory.getServiceContext(
                                                                             "/etc/grid-security/hostcert.pem",
                                                                             "/etc/grid-security/hostkey.pem",
                                                                             "/etc/grid-security/certificates");
    }

    private GridftpClient client;


    public void gridFTPRead(RemoteGsiftpTransferProtocolInfo remoteGsiftpProtocolInfo, StorageInfo storage, PnfsId pnfsId, final SpaceMonitor spaceMonitor, GSSCredential deleg_cred) throws Exception {
        try {
            GlobusURL src_url =  new GlobusURL(remoteGsiftpProtocolInfo.getGsiftpUrl());
            boolean emode = remoteGsiftpProtocolInfo.isEmode();
            long size = client.getSize(src_url.getPath());
            say(" received a file size info: "+size+" allocating space on the pool");
            spaceMonitor.allocateSpace(size);
            say(" allocated space " + size);

            DiskDataSourceSink sink =
                new DiskDataSourceSink(remoteGsiftpProtocolInfo.getBufferSize(),
                                       false);
            boolean freedAll = false;
            try {
                client.gridFTPRead(src_url.getPath(),sink, emode);
            } catch (Exception e) {
                esay("gridFTPRead: error : ");
                esay(e);
                spaceMonitor.freeSpace(size);
                freedAll = true;
                throw e;
            } finally {
                //
                // we need to return the space if something went wrong.  -p.
                //
                long realSize = sink.length();
                client.close();

                if (!freedAll) {
                    //
                    // overallocated
                    //
                    if (realSize < size){
                        long toBeReturned = size - realSize;
                        say("Returning space : "+toBeReturned);
                        spaceMonitor.freeSpace(toBeReturned);
                    } else  if (realSize > size) {
                        long toBeAllocated = realSize - size;
                        say("Allocating more space : "+toBeAllocated);
                        spaceMonitor.allocateSpace(toBeAllocated);
                    }
                }
                //                client = null;
            }
            /*
              GridftpClient client;
              return;*/

        } catch (Exception e) {
            throw new CacheException(e.toString());
        }
    }

    public void gridFTPWrite(RemoteGsiftpTransferProtocolInfo remoteGsiftpProtocolInfo, StorageInfo storage, PnfsId pnfsId, final SpaceMonitor spaceMonitor, GSSCredential deleg_cred) throws Exception {
        say("gridFTPWrite started");

        try {

            GlobusURL dst_url =  new GlobusURL(remoteGsiftpProtocolInfo.getGsiftpUrl());
            boolean emode = remoteGsiftpProtocolInfo.isEmode();

            try {
                DiskDataSourceSink source =
                    new DiskDataSourceSink(remoteGsiftpProtocolInfo.getBufferSize(),
                                           true);
                client.gridFTPWrite(source,
                                    dst_url.getPath(), emode,  true);
            } finally {
                client.close();
            }

        } catch (Exception e) {
            esay("gridFtpWrite exception");
            esay(e);
            throw new CacheException(e.toString());
        }
    }

    // the following methods were adapted from DCapProtocol_3_nio mover
    public Checksum getClientChecksum() {
        return null;
    }

    public Checksum getTransferChecksum() {
        try {
            if (_transferChecksum == null) {
                return null;
            }

            if (recalculateOnFile && _transferMessageDigest != null) {
                byte[] bytes = new byte[128*1024];
                raDiskFile.seek(0);
                while (true) {
                    int read = raDiskFile.read(bytes);
                    if (read <= 0) {
                        break;
                    }
                    _transferMessageDigest.update(bytes,0,read);
                }
            }

            return _transferChecksum;
        } catch (Exception e){
            esay(e);
            return null;
        }
    }

    public ChecksumFactory getChecksumFactory(ProtocolInfo protocol) { return null; }

    public void setDigest(Checksum checksum) {
        _transferChecksum      =  checksum;
        _transferMessageDigest =
            checksum != null? checksum.getMessageDigest() : null;

    }

    public synchronized void receiveEBlock(byte[] array,
                                           int offset,
                                           int length,
                                           long offsetOfArrayInFile)
        throws IOException {
        raDiskFile.seek(offsetOfArrayInFile);
        raDiskFile.write(array, offset, length);

        if (_transferMessageDigest == null || recalculateOnFile) {

            return;
        }

        if (_transferMessageDigest != null && previousUpdateEndOffset !=offsetOfArrayInFile) {
            say("previousUpdateEndOffset="+previousUpdateEndOffset+
                " offsetOfArrayInFile="+offsetOfArrayInFile+
                " : resetting the digest for future checksum calculation of the file");
            recalculateOnFile = true;
            _transferMessageDigest.reset();
            return;

        }

        if (array == null){
            return;
        }
        previousUpdateEndOffset += length;
        if (_transferMessageDigest != null) {
            _transferMessageDigest.update(array,offset,length);
        }
    }



    private class DiskDataSourceSink implements IDiskDataSourceSink {
        private final int buf_size;
        private volatile long last_transfer_time = System.currentTimeMillis();
        private long transfered = 0;
        private boolean source;

        public DiskDataSourceSink(int buf_size,boolean source) {
            this.buf_size = buf_size;
            this.source = source;
        }

        public synchronized void write(Buffer buffer) throws IOException {
            if (source) {
                String error = "DiskDataSourceSink is source and write is called";
                esay(error);
                throw new IllegalStateException(error);
            }
            //say("DiskDataSourceSink.write()");

            last_transfer_time    = System.currentTimeMillis();
            int read = buffer.getLength();
            long offset = buffer.getOffset();
            if (offset >= 0) {
                receiveEBlock(buffer.getBuffer(),
                              0, read,
                              buffer.getOffset());
            } else {
                //this is the case when offset is not supported
                // for example reading from a stream
                receiveEBlock(buffer.getBuffer(),
                              0, read,
                              transfered);

            }
            transfered +=read;
        }

        public void close()
            throws IOException {
            say("DiskDataSink.close() called");
            last_transfer_time    = System.currentTimeMillis();
        }

        /** Getter for property last_transfer_time.
         * @return Value of property last_transfer_time.
         *
         */
        public long getLast_transfer_time() {
            return last_transfer_time;
        }

        /** Getter for property transfered.
         * @return Value of property transfered.
         *
         */
        public synchronized long getTransfered() {
            return transfered;
        }

        public synchronized Buffer read() throws IOException {
            if (!source) {
                String error = "DiskDataSourceSink is sink and read is called";
                esay(error);
                throw new IllegalStateException(error);
            }
            //say("DiskDataSourceSink.read()");

            last_transfer_time    = System.currentTimeMillis();
            byte[] bytes = new byte[buf_size];

            int read = raDiskFile.read(bytes);
            //say("DiskDataSourceSink.read() read "+read+" bytes");
            if (read == -1) {
                return null;
            }
            Buffer buffer = new Buffer(bytes,read,transfered);
            transfered  += read;
            return buffer;
        }

        public String getCksmValue(String type) throws IOException,NoSuchAlgorithmException {
            if (!type.toLowerCase().equals("adler32"))
                throw new NoSuchAlgorithmException("RemoteGsiftpTransferProtocol: getChecksumValue supports only adler32");
            long value = getAdler32();
            value |=0x100000000L;
            value &=0x1ffffffffL;
            String svalue = Long.toHexString(value);
            return svalue.substring(1);
        }


        public long getAdler32() throws IOException{
            try {
                PnfsHandler pnfsHandler = new PnfsHandler(cell,new CellPath("PnfsManager"));
                String adler32String = pnfsHandler.getPnfsFlag(pnfsId, "c");
                if (adler32String.startsWith("1:")) {
                    adler32String = adler32String.substring(2);
                    say("adler32 read from pnfs for file "+pnfsId+" is "+adler32String);
                    return Long.parseLong(adler32String,16);
                }

            } catch (Exception e){
                esay("could not get adler32 from pnfs:");
                esay(e);
                esay("ignoring this error");

            }
            long adler32 = GridftpClient.getAdler32(raDiskFile);
            say("adler 32 for file "+raDiskFile+" is "+adler32);
            raDiskFile.seek(0);
            return adler32;
        }

        public long length() throws IOException{
            return raDiskFile.length();
        }

    }


}
