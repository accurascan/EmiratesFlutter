#ifndef FACEENGINE_H
#define FACEENGINE_H

#include "types.h"


//.initialize SDK,
SResult InitEngine(const char* file1, const char* file2, const char* license);

//.Close SDK
SResult CloseEngine();

//. Detect all features.
SResult DetectSourceFace(BYTE* pImgRGBBuff, DWORD wuImgSize, DWORD wuImgWidth,DWORD wuImgHeight, int *pFaceCount, SFaceExt *parFaces);
SResult DetectTargetFace(BYTE* pImgRGBBuff, DWORD wuImgSize, DWORD wuImgWidth,DWORD wuImgHeight, int *pFaceCount, SFaceExt *parFaces, float* pFeature1);

SResult Identify(int nSize1, float* pFeature1, int nSize2, float* pFeature2, float* pScore);

//. Delete all template.
SResult DeleteAllTemplate();

#endif // FACEENGINE_H
