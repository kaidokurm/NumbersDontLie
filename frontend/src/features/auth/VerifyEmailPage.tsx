import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Alert } from '../../shared/ui/Alert';
import { Spinner } from '../../shared/ui/Spinner';
import { Card } from '../../shared/ui/Card';
import { resendVerificationCode, verifyEmail } from '../../api/auth';

export function VerifyEmailPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    const email = searchParams.get('email');
    const code = searchParams.get('code');

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);
    const [resendLoading, setResendLoading] = useState(false);
    const [resendMessage, setResendMessage] = useState<string | null>(null);

    useEffect(() => {
        const verify = async () => {
            if (!email || !code) {
                setError('Invalid verification link');
                setLoading(false);
                return;
            }

            try {
                await verifyEmail(email, code);

                setSuccess(true);
                // Redirect to login after 2 seconds
                setTimeout(() => navigate('/'), 2000);
            } catch (err) {
                setError(err instanceof Error ? err.message : 'Verification failed');
            } finally {
                setLoading(false);
            }
        };

        verify();
    }, [email, code, navigate]);

    const handleResendCode = async () => {
        if (!email) {
            setError('Cannot resend code without email in the link');
            return;
        }
        setResendLoading(true);
        setResendMessage(null);
        try {
            const response = await resendVerificationCode(email);
            setResendMessage(response.message);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Could not resend verification code');
        } finally {
            setResendLoading(false);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-linear-to-b from-blue-50 to-white p-4">
            <Card>
                <div className="p-8 text-center">
                    {loading && (
                        <>
                            <h2 className="text-2xl font-bold text-slate-800 mb-4">Verifying Email</h2>
                            <Spinner  />
                            <p className="text-slate-600">Please wait...</p>
                        </>
                    )}

                    {success && (
                        <>
                            <div className="text-4xl mb-4">✓</div>
                            <h2 className="text-2xl font-bold text-green-600 mb-2">Email Verified!</h2>
                            <p className="text-slate-600 mb-4">Your email has been verified successfully.</p>
                            <p className="text-sm text-slate-500">Redirecting to login...</p>
                        </>
                    )}

                    {error && !loading && (
                        <>
                            <h2 className="text-2xl font-bold text-slate-800 mb-4">Verification Failed</h2>
                            <Alert tone="error" title="Error" message={error} />
                            {resendMessage && <Alert tone="success" title="Code Sent" message={resendMessage} />}
                            {email && (
                                <button
                                    onClick={handleResendCode}
                                    disabled={resendLoading}
                                    className="mt-4 mb-3 text-blue-700 hover:text-blue-800 font-semibold disabled:opacity-50"
                                >
                                    {resendLoading ? 'Resending...' : 'Resend Verification Code'}
                                </button>
                            )}
                            <button
                                onClick={() => navigate('/')}
                                className="text-green-600 hover:text-green-700 font-semibold"
                            >
                                Return to Login
                            </button>
                        </>
                    )}
                </div>
            </Card>
        </div>
    );
}
