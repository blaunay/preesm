#include "jobQueue.h"

#define BUFSIZE 1024
#define PIPE_TIMEOUT 5000


void printLastMessage(){
	LPCWSTR lpMsgBuf;
	FormatMessage(
		FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM, NULL,
		GetLastError(),
		MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default language
		(LPTSTR) &lpMsgBuf, 0, NULL );// Display the string.
	::MessageBox( NULL, lpMsgBuf, _T("GetLastError"), MB_OK|MB_ICONINFORMATION );
	LocalFree( lpMsgBuf );
	::MessageBox ( NULL, _T("Le tube de communication n'a pas pu �tre cr��"),_T("Erreur"),MB_ICONERROR |MB_OK) ;
	PostQuitMessage (0) ;
}

JobQueue::JobQueue()
{
}

JobQueue::JobQueue(string type)
{
	LPTSTR lpszPipename = LPTSTR("\\\\.\\pipe\\jobQueuePipe"); 
    DWORD dw = GetLastError();

	if(type == "master"){
		this->namedPipe = CreateNamedPipe (_T("\\\\.\\pipe\\jobQueuePipe"),
PIPE_ACCESS_OUTBOUND | FILE_FLAG_WRITE_THROUGH,
PIPE_TYPE_MESSAGE | PIPE_WAIT,
1,BUFSIZE,BUFSIZE,PIPE_TIMEOUT,NULL) ;

if (this->namedPipe == INVALID_HANDLE_VALUE)
{
	printLastMessage();
}


	}
	else{
		this->namedPipe = CreateFile (
			lpszPipename, // Nom du tube
			GENERIC_READ, // Mode d'acc�s (lecture/�criture)
			// GENERIC_READ si le tube a �t� cr�� avec PIPE_ACCESS_OUTBOUND
			//GENERIC_WRITE si le tube a �t� cr�� avec PIPE_ACCESS_INBOND
			// GENERIC_WRITE | GENERIC_READ si duplex
			0, // 0 pour interdire le partage du tube avec d'autres processus
			NULL, // Privil�ge d'acc�s
			OPEN_EXISTING, // OPEN_EXISTING puisqu'on ouvre un tube existant
			FILE_FLAG_WRITE_THROUGH, //FILE_FLAG_WRITE_THROUGH pour une �criture synchrone
			NULL // Pas de signification pour les tubes
		);
	}
}