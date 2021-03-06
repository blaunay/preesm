/*!
 * File generated by platform wizard. DO NOT MODIFY
 *
 */

metaonly module Platform inherits xdc.platform.IPlatform {

    config ti.platforms.generic.Platform.Instance CPU =
        ti.platforms.generic.Platform.create("CPU", {
            clockRate:      1000,                                       
            catalogName:    "ti.catalog.c6000",
            deviceName:     "TMS320C6678",
            customMemoryMap:
           [          
                ["MSMCSRAM", 
                     {
                        name: "MSMCSRAM",
                        base: 0x0c000000,                    
                        len: 0x00200000,                    
                        space: "code/data",
                        access: "RWX",
                     }
                ],
                ["L2SRAM", 
                     {
                        name: "L2SRAM",
                        base: 0x00800000,                    
                        len: 0x00080000,                    
                        space: "code/data",
                        access: "RWX",
                     }
                ],
                ["SHARED_NO_CACHE", 
                     {
                        name: "SHARED_NO_CACHE",
                        base: 0x80000000,                    
                        len: 0x80000000,                    
                        space: "code/data",
                        access: "RWX",
                     }
                ],
           ],
          l2Mode:"0k",
          l1PMode:"32k",
          l1DMode:"32k",

    });
    
instance :
    
    override config string codeMemory  = "L2SRAM";   
    override config string dataMemory  = "L2SRAM";                                
    override config string stackMemory = "L2SRAM";
    
}
