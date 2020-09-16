#ifndef _USERYPES_H
#define _USERYPES_H


#include <stdint.h>

#define IN
#define OUT
//#define NULL  0

#define   TRUE    1
#define   FALSE   0

#ifndef max
#define max(a,b)            (((a) > (b)) ? (a) : (b))
#endif

#ifndef min
#define min(a,b)            (((a) < (b)) ? (a) : (b))
#endif


typedef unsigned char		BYTE,		*PBYTE;
typedef unsigned short		WORD ,          *PWORD;
typedef __uint32_t		DWORD,		*PDWORD;
typedef void		                        *PVOID;
typedef float		                        *PFLOAT;

/*
//////////////////////////////////////////////////////////////////////////
// signed types
typedef char				WChar,		*WPChar,		**WPPChar;			// 1
typedef short				WShort,		*WPShort,		**WPPShort;			// 2
typedef int					WBool,		*WPBool,		**WPPBool;			// 4
typedef int					WInt,		*WPInt,			**WPPInt;			// 4
typedef float				WFloat,		*WPFloat,		**WPPFloat;			// 4
typedef double				WDouble,	*WPDouble,		**WPPDouble;		// 8
#if defined(WIN32) || defined(_WIN32_WCE)
	typedef __int64         WInt64,         *WPInt64,		**WPPInt64;     //8
#else
	typedef int64         WInt64,         *WPInt64,		**WPPInt64;     //8
#endif 
typedef long double			WLDouble,	*WPLDouble,		**WPPLDouble;		// 10
//////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////
// unsigned types
typedef unsigned char		WByte,		*BYTE*,		**WPPByte;			// 1
typedef unsigned short		WUShort,	*WPUShort,		**WPPUShort;		// 4
typedef unsigned int		WUInt,		*WPUInt,		**WPPUInt;			// 4
#if defined(WIN32) || defined(_WIN32_WCE)
typedef unsigned __int64    WUInt64,    *WPUInt64,		**WPPUInt64;        //8
#else
typedef uint64			WUInt64,	*WPUInt64,		**WPPUInt64;        //8
#endif 

//////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////////////////
typedef unsigned int		WSizeType,	*WPSizeType,	**WPPSizeType;		// 4
typedef void				WVoid,		*WPVoid,		**WPPVoid;
//////////////////////////////////////////////////////////////////////////
*/

#pragma pack (4)
typedef enum
{
	ENROLL = 0,
	MATCHING
}TEMPLATEYPE;

typedef struct WRect_
{
	int X;
	int Y;
	int Width;
	int Height;
} SWRect, *PSWRect;

typedef struct SWPoint_
{
	int X;
	int Y;
}SWPoint, *PSWPoint;

typedef struct SFaceRotation_
{
	short Yaw;
	short Pitch;
	short Roll;
}SFaceRotation, *PSFaceRotation;

typedef struct SFace_
{
	SWRect Rectangle;//16,
	SFaceRotation Rotation;//6,(8)
	double Confidence;//8
} SFace, *PSFace; //32

typedef struct SEyes_
{
	SWPoint First;	//8
	double FirstConfidence; //8
	SWPoint Second;//8
	double SecondConfidence;//8
} SEyes, *PSEyes;

typedef struct SFeaturePoint_
{
	WORD Code; //2
	WORD X; //2
	WORD Y;//2
	BYTE Confidence;//1
}SFeaturePoint, *PSFeaturePoint;


typedef struct SFaceDetectionDetails_
{
	BOOL FaceAvailable; //8
	SFace Face;	//32
	BOOL EyesAvailable; //8
	SEyes Eyes;//32
	SFeaturePoint RightEyeCenter;//8==88
	SFeaturePoint LeftEyeCenter;//8==96
	SFeaturePoint NoseTip;//8
	SFeaturePoint MouthCenter;//8
	SFeaturePoint Reserved[4];//32
} SFaceDetectionDetails, *PSFaceDetectionDetails;

typedef struct SFaceDetails_
{
	SWPoint RightEye;	//8
	SWPoint LeftEye;	//8
	//SWPoint Nose;	//8
	SWPoint Mouth;	//8
} SFaceDetails, *PSFaceDetails;

typedef struct SFaceExt_
{
	SWRect Rectangle;//16,
	double Confidence;//8
    int nFeatureSize;
    float featureData[128];
    DWORD dwNewWidth;
    DWORD dwNewHeight;
    BYTE* pNewImg;
} SFaceExt, *PSFaceExt; //32

typedef enum SResult_
{
	wOK = 0,
	wEngineNotInit = -1,
	wEngineAlready = -2,
	wImageNotFormat = -3,
	wFaceNotDetected = -4,
	wEyesNotDetected = -5,
	wTemplateNotCreated = -6,
	wTemplateNotIdentify = -7,
	wMINIODNotSETTING = -8,
	wMAXIODNotSETTING = -9,
	wMINMAXIODNotSETTING = -10,
	wGrayImageNotCreated = -11,
	wErrorParameter      = -12,
	wErrorMemoryCreated  = -13,
	wErrorImageProc  = -14,
	wErrorLicence  = -15,
	wErrorLicenceDay = -16

} SResult;

#define JOB_ENROLL			0
#define JOB_IDENTIFY		1


#define WFalse	FALSE
#define WTrue	TRUE

#endif 
