#pragma once
#ifndef GEORGERINGO_HPP_INCLUDED
#define GEORGERINGO_HPP_INCLUDED

// define GEORGERINGO_DLL when building libgerogreringo.dll
# if defined(_WIN32) && !defined(__GNUC__)
#  ifdef GEORGERINGO_DLL
#   define GEORGERINGO_DECL __declspec(dllexport)
#  else
#   define GEORGERINGO_DECL __declspec(dllimport)
#  endif 
# endif // WIN32

#ifndef GEORGERINGO_DECL
# define GEORGERINGO_DECL
#endif

GEORGERINGO_DECL void georgeringo();


#endif // GEORGERINGO_HPP_INCLUDED
