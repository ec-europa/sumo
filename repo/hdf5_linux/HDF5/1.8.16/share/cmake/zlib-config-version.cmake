#-----------------------------------------------------------------------------
# ZLIB Version file for install directory
#-----------------------------------------------------------------------------

set (PACKAGE_VERSION 1.2)

if ("${PACKAGE_FIND_VERSION_MAJOR}" EQUAL 1.2)

  # exact match for version 1.2.8
  if ("${PACKAGE_FIND_VERSION_MINOR}" EQUAL 8)

    # compatible with any version 1.2.8.x
    set (PACKAGE_VERSION_COMPATIBLE 1) 
    
    if ("${PACKAGE_FIND_VERSION_PATCH}" EQUAL )
      set (PACKAGE_VERSION_EXACT 1)    

      if ("${PACKAGE_FIND_VERSION_TWEAK}" EQUAL )
        # not using this yet
      endif ("${PACKAGE_FIND_VERSION_TWEAK}" EQUAL )
      
    endif ("${PACKAGE_FIND_VERSION_PATCH}" EQUAL )
    
  endif ("${PACKAGE_FIND_VERSION_MINOR}" EQUAL 8)
endif ("${PACKAGE_FIND_VERSION_MAJOR}" EQUAL 1.2)


