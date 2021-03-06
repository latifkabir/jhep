/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.jnp.reader;

import java.util.List;
import java.util.Objects;
import org.jlab.jnp.hipo.data.HipoEvent;
import org.jlab.jnp.hipo.data.HipoGroup;
import org.jlab.jnp.hipo.data.HipoNode;
import org.jlab.jnp.hipo.data.HipoNodeType;
import org.jlab.jnp.hipo.io.DataBankHipo;
import org.jlab.jnp.hipo.io.DataEventHipo;
import org.jlab.jnp.hipo.io.HipoReader;
import org.jlab.jnp.pdg.PDGDatabase;
import org.jlab.jnp.utils.options.OptionParser;
import org.jlab.jnp.physics.Particle;
import org.jlab.jnp.physics.ParticleList;
import org.jlab.jnp.physics.PhysicsEvent;
import org.jlab.jnp.physics.map.BaseMapProducer;
import org.jlab.jnp.processes.SIDIS;

/**
 *
 * @author gavalian
 */
public class EventReader {
    
    HipoReader  reader = null;
    Integer     numberOfEvents = 0;
    Integer     currentEvent   = 0;
    HipoEvent   hipoEvent      = null;
    
    private     DataEventHipo  dataEventHipo = new DataEventHipo();
    private     String       mcEventBankName = "mc::event";
    private     String     dataEventBankName = "data::event";
    
    
    public EventReader(){
        
    }
    
    public void open(String file){
        reader = new HipoReader();
        reader.open(file);
        numberOfEvents = reader.getEventCount();
        currentEvent   = 0;
    }
    
    public Boolean nextEvent(){
        if(Objects.equals(currentEvent, numberOfEvents)) return false;
        hipoEvent = reader.readEvent(currentEvent);
        currentEvent++;
        return true;
    }
    
    public static boolean readPhysicsEvent(DataEventHipo event, PhysicsEvent physEvent, String bankName){
        
        DataBankHipo bank = new DataBankHipo();
        event.getDataBank(bank, bankName);
        int pidSize = event.getSize(2104426497);// bank.getSize("pid");
        physEvent.resize(pidSize);
        int pid_pos = event.getPosition(2104426497);
        int  px_pos = event.getPosition(2104426498);
        int  py_pos = event.getPosition(2104426499);
        int  pz_pos = event.getPosition(2104426500);
        int  vx_pos = event.getPosition(2104426501);
        int  vy_pos = event.getPosition(2104426502);
        int  vz_pos = event.getPosition(2104426503);
        
        for(int p = 0; p < pidSize; p++){
            int pid = event.getIntAt(pid_pos, p);
            if(PDGDatabase.hasParticleById(pid)==true){
                physEvent.getParticle(p).initParticle(pid,
                        event.getDoubleAt(px_pos,p),
                        event.getDoubleAt(py_pos,p),
                        event.getDoubleAt(pz_pos,p),
                        event.getDoubleAt(vx_pos,p),
                        event.getDoubleAt(vy_pos,p),
                        event.getDoubleAt(vz_pos,p)
                );
            } else {
                physEvent.getParticle(p).initParticleWithMass(0.135,
                        event.getDoubleAt(px_pos,p),
                        event.getDoubleAt(py_pos,p),
                        event.getDoubleAt(pz_pos,p),
                        event.getDoubleAt(vx_pos,p),
                        event.getDoubleAt(vy_pos,p),
                        event.getDoubleAt(vz_pos,p)
                );
                /*physEvent.getParticle(p).initParticleWithMass(0.135,
                        bank.getDouble("px",p),
                        bank.getDouble("py",p),
                        bank.getDouble("pz",p),
                        bank.getDouble("vx",p),
                        bank.getDouble("vy",p),
                        bank.getDouble("vz",p)
                        );*/
            }
        }
        return true;
    }
    
    public Boolean getMcEvent(PhysicsEvent event){
     
        event.clear();

        if(hipoEvent.hasGroup("mc::header")==true){
            HipoGroup header = hipoEvent.getGroup("mc::header");
            double weight = header.getNode("weight").getFloat(0);
            event.setWeight(weight);
            HipoNode nodeP = header.getNode("parameters");
            for(int i = 0; i < nodeP.getDataSize(); i++){
                event.setParameter(i, nodeP.getFloat(i));
            }
        }
        
        if(hipoEvent.hasGroup("mc::event")==false) return false;
        HipoGroup group = hipoEvent.getGroup("mc::event");
        int nrows = group.getMaxSize();
        for(int i = 0; i < nrows; i++){
            int status = group.getNode("status").getByte(i);
            int pid    = group.getNode("pid").getShort(i);
            int parent = group.getNode("parent").getByte(i);
            if(status==1){
                Particle p = new Particle(pid,
                        group.getNode("px").getFloat(i),
                        group.getNode("py").getFloat(i),
                        group.getNode("pz").getFloat(i),
                        group.getNode("vx").getFloat(i),
                        group.getNode("vy").getFloat(i),
                        group.getNode("vz").getFloat(i)
                );
                event.addParticle(p);
            }
            if(status==0&&parent==0&&pid==11){
                Particle p = new Particle(pid,
                        group.getNode("px").getFloat(i),
                        group.getNode("py").getFloat(i),
                        group.getNode("pz").getFloat(i),
                        group.getNode("vx").getFloat(i),
                        group.getNode("vy").getFloat(i),
                        group.getNode("vz").getFloat(i)
                );
                event.setBeamParticle(p);
            }
            if(status==0&&parent==0&&pid==2212){
                Particle p = new Particle(pid,
                        group.getNode("px").getFloat(i),
                        group.getNode("py").getFloat(i),
                        group.getNode("pz").getFloat(i),
                        group.getNode("vx").getFloat(i),
                        group.getNode("vy").getFloat(i),
                        group.getNode("vz").getFloat(i)
                );
                event.setTargetParticle(p);
            }
            //if()
        }
        

        return true;
    }
    
    public boolean getDataEvent(PhysicsEvent event){
        event.clear();
        if(hipoEvent.hasGroup("data::event")==false) return false;
        HipoGroup group = hipoEvent.getGroup("data::event");
        int nrows = group.getMaxSize();
        for(int i = 0; i < nrows; i++){
            int status = group.getNode("status").getByte(i);
            int pid    = group.getNode("pid").getInt(i);
            Particle p = new Particle(pid,
                        group.getNode("px").getFloat(i),
                        group.getNode("py").getFloat(i),
                        group.getNode("pz").getFloat(i),
                        group.getNode("vx").getFloat(i),
                        group.getNode("vy").getFloat(i),
                        group.getNode("vz").getFloat(i)
                );            
            event.addParticle(p);
        }
        return true;
    }
    
    
    public static PhysicsEvent  createPhysicsEvent(ParticleList list){
        int count = list.count();
        PhysicsEvent event = new PhysicsEvent();
        for(int i = 0; i < count; i++) event.addParticle(list.get(i));
        return event;
    }
    
    public static ParticleList readParticleList(HipoEvent event, String bankname){
        ParticleList plist = new ParticleList();
        if(event.hasGroup(bankname)==true){
            HipoGroup group = event.getGroup(bankname);
            int nrows = group.getMaxSize();
            for(int i = 0; i < nrows; i++){
                int   pid = 0;
                if(group.getNode("pid").getType()==HipoNodeType.INT){
                    pid = group.getNode("pid").getInt(i);
                }
                if(group.getNode("pid").getType()==HipoNodeType.SHORT){
                    pid = group.getNode("pid").getShort(i);
                }
                
                if(PDGDatabase.hasParticleById(pid)==true){
                    Particle part = new Particle(pid,
                            group.getNode("px").getFloat(i),
                            group.getNode("py").getFloat(i),
                            group.getNode("pz").getFloat(i),
                            group.getNode("vx").getFloat(i),
                            group.getNode("vy").getFloat(i),
                            group.getNode("vz").getFloat(i)
                    );
                    plist.add(part);
                } else {
                    Particle part = new Particle();
                    
                    part.initParticleWithMass(0.135, 
                            group.getNode("px").getFloat(i),
                            group.getNode("py").getFloat(i),
                            group.getNode("pz").getFloat(i),
                            group.getNode("vx").getFloat(i),
                            group.getNode("vy").getFloat(i),
                            group.getNode("vz").getFloat(i));                    
                    plist.add(part);
                }                
            }
        }
        return plist;
    }
    
    
    public static void main(String[] args){
        
        OptionParser parser = new OptionParser();
        parser.parse(args);
        List<String> inputFiles = parser.getInputList();
        PhysicsEvent mcEvent = new PhysicsEvent();
        
        BaseMapProducer mapProducer = new BaseMapProducer();
        mapProducer.setFilter("11:X+:X-:Xn");
        
        
        mapProducer.addParticle("phi", "[321]+[-321]");
        mapProducer.addProperty("phi", "mass");
        mapProducer.addProperty("phi", "px");
        mapProducer.addProperty("phi", "py");
        
        mapProducer.addParticle("L0", "[2212]+[-211]");
        mapProducer.addProperty("L0", "mass");
        
        mapProducer.addParticle("K0", "[211]+[-211]");
        mapProducer.addProperty("K0", "mass");
        mapProducer.addParticle("L01520", "[2112]+[211]+[-211]");
        mapProducer.addProperty("L01520", "mass");
        
        SIDIS sidis = new SIDIS();
        
        for(String item : inputFiles){
            EventReader reader = new EventReader();
            reader.open(item);
            while(reader.nextEvent()==true){
                reader.getMcEvent(mcEvent);
                /*if(mapProducer.processPhysicsEvent(mcEvent)==true){
                    //System.out.println(mcEvent.toLundString());
                    //System.out.println(mapProducer.getMap().get("phi__mass"));
                    System.out.println(mapProducer.toString());
                }*/
                if(sidis.processPhysicsEvent(mcEvent)==true){
                    //System.out.println(mcEvent.toLundString());
                    //System.out.println(mapProducer.getMap().get("phi__mass"));
                    System.out.println(sidis.toString());
                }
            }
        }
    }
}
